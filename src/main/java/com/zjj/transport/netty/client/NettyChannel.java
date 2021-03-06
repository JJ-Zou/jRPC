package com.zjj.transport.netty.client;

import com.zjj.common.JRpcURL;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import com.zjj.transport.TransChannel;
import com.zjj.transport.netty.ChannelState;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;


@Slf4j
public class NettyChannel implements TransChannel {

    private volatile ChannelState state = ChannelState.UNINITIALIZED;

    private final NettyClient nettyClient;

    private Channel channel;

    private InetSocketAddress localAddress;
    private final InetSocketAddress remoteAddress;

    public NettyChannel(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
        this.remoteAddress = nettyClient.getRemoteAddress();
    }

    @Override
    public Response request(Request request) throws IOException {
        //todo:
        return null;
    }

    @Override
    public boolean open() {
        if (isAvailable()) {
            log.warn("NettyChannel {} has already open.", this);
            return true;
        }
        try {
            this.channel = nettyClient.connect().syncUninterruptibly().channel();
            this.localAddress = (InetSocketAddress) channel.localAddress();
            log.warn("NettyChannel {} has started success.", channel);
            state = ChannelState.ACTIVE;
        } catch (Exception e) {
            log.warn("NettyChannel {} has occurred exception.", this, e);
            state = ChannelState.INACTIVE;
        }
        return channel.isActive();
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        if (channel != null) {
            channel.close();
        }
        state = ChannelState.CLOSED;
    }

    @Override
    public boolean isClosed() {
        return state.isClosed();
    }

    @Override
    public boolean isAvailable() {
        return state.isActive();
    }

    @Override
    public JRpcURL getUrl() {
        return nettyClient.getUrl();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
