package com.zjj.jrpc.transport.support;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.exception.JRpcErrorMessage;
import com.zjj.jrpc.exception.JRpcFrameworkException;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.transport.Client;
import com.zjj.jrpc.transport.TransChannel;
import com.zjj.jrpc.transport.netty.client.NettyChannel;
import com.zjj.jrpc.transport.netty.client.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public abstract class AbstractClient implements Client {
    protected final InetSocketAddress remoteAddress;
    private final AtomicInteger idx = new AtomicInteger(0);
    private final TransChannel[] transChannels;
    private final int clientInitConnections;
    protected InetSocketAddress localAddress;
    protected JRpcURL url;

    protected AbstractClient(JRpcURL url) {
        this.url = url;
        this.remoteAddress = new InetSocketAddress(url.getHost(), url.getPort());
        this.clientInitConnections = url.getParameter(JRpcURLParamType.CLIENT_INIT_CONNECTIONS.getName(), JRpcURLParamType.CLIENT_INIT_CONNECTIONS.getIntValue());
        if (clientInitConnections <= 0) {
            throw new JRpcFrameworkException("clientInitConnections = 0.", JRpcErrorMessage.FRAMEWORK_INIT_ERROR);
        }
        this.transChannels = new TransChannel[clientInitConnections];
    }

    protected void initialize() {
        for (int i = 0; i < clientInitConnections; i++) {
            transChannels[i] = new NettyChannel((NettyClient) this);
            try {
                transChannels[i].open();
            } catch (Exception e) {
                log.error("{} connect err.", transChannels[i], e);
            }
        }
    }

    protected TransChannel obtainActiveChannel() {
        int callId = idx.incrementAndGet();
        int startId = (callId & 0x0fffffff) % clientInitConnections;
        int index = startId;
        do {
            if (!transChannels[index].isAvailable()) {
                transChannels[index] = new NettyChannel((NettyClient) this);
            }
            if (transChannels[index].isAvailable()) {
                log.info("Client obtain an available channel {}", transChannels[index]);
                return transChannels[index];
            }
            index = (index + 1) % clientInitConnections;
        } while (index != startId);
        return null;
    }

    protected void closeAll() {
        for (TransChannel transChannel : transChannels) {
            if (transChannel != null) {
                transChannel.close();
            }
        }
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public void heartbeat(Request request) {
        throw new JRpcFrameworkException(this.getClass().getName() + " method heartbeat unsupported " + request);
    }

    @Override
    public JRpcURL getUrl() {
        return url;
    }
}
