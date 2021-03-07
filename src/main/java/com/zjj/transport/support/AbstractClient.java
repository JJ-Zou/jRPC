package com.zjj.transport.support;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.exception.JRpcErrorMessage;
import com.zjj.exception.JRpcFrameworkException;
import com.zjj.rpc.Request;
import com.zjj.transport.Client;
import com.zjj.transport.TransChannel;
import com.zjj.transport.netty.client.NettyChannel;
import com.zjj.transport.netty.client.NettyClient;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public abstract class AbstractClient implements Client {
    protected InetSocketAddress localAddress;
    protected final InetSocketAddress remoteAddress;

    private final AtomicInteger idx = new AtomicInteger(0);
    protected JRpcURL url;

    private final TransChannel[] transChannels;

    private final int clientInitConnections;

    protected AbstractClient(JRpcURL url) {
        this.url = url;
        this.remoteAddress = new InetSocketAddress(url.getHost(), url.getPort());
        this.clientInitConnections = url.getParameter(JRpcURLParamType.clientInitConnections.getName(), JRpcURLParamType.clientInitConnections.getIntValue());
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

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
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
