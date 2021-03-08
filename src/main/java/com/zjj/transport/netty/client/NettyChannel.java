package com.zjj.transport.netty.client;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.exception.JRpcErrorMessage;
import com.zjj.exception.JRpcFrameworkException;
import com.zjj.exception.JRpcServiceConsumerException;
import com.zjj.exception.JRpcServiceProviderException;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import com.zjj.rpc.ResponseFuture;
import com.zjj.rpc.support.DefaultResponseFuture;
import com.zjj.transport.TransChannel;
import com.zjj.transport.netty.ChannelState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
        int requestTimeout = getUrl().getMethodParameter(request.getMethodName(), request.getParameterSign(),
                JRpcURLParamType.requestTimeout.getName(), JRpcURLParamType.requestTimeout.getIntValue());
        if (requestTimeout <= 0) {
            throw new JRpcFrameworkException("method request timeout cannot less than or equal zero.", JRpcErrorMessage.FRAMEWORK_INIT_ERROR);
        }
        ResponseFuture<?> responseFuture = new DefaultResponseFuture<>(getUrl(), requestTimeout, request);
        nettyClient.registerCallback(request.getRequestId(), responseFuture);
        ChannelFuture writeFuture = channel.writeAndFlush(request);
        boolean result = writeFuture.awaitUninterruptibly(requestTimeout);
        if (result) {
            responseFuture.addListener(f -> {
                if (f.isSuccess() || (responseFuture.getException() instanceof JRpcServiceConsumerException)) {
// TODO: 2021/3/8 成功回调
                    log.info("成功回调");
                } else {
// TODO: 2021/3/8 失败回调
                    log.info("失败回调");
                }
            });
            return responseFuture;
        }
        writeFuture.cancel(true);
        responseFuture.cancel();
        log.error("NettyChannel request [{}] error, url: [{}]", request, getUrl());
        throw new JRpcServiceProviderException(writeFuture.cause());
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

    @Override
    public String toString() {
        return "NettyChannel{" +
                "state=" + state +
                ", nettyClient=" + nettyClient +
                ", channel=" + channel +
                ", localAddress=" + localAddress +
                ", remoteAddress=" + remoteAddress +
                '}';
    }
}
