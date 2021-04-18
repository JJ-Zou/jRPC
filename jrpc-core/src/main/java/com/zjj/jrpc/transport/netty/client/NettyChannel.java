package com.zjj.jrpc.transport.netty.client;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.exception.JRpcErrorMessage;
import com.zjj.jrpc.exception.JRpcFrameworkException;
import com.zjj.jrpc.exception.JRpcServiceConsumerException;
import com.zjj.jrpc.exception.JRpcServiceProviderException;
import com.zjj.jrpc.extension.ExtensionLoader;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.rpc.Response;
import com.zjj.jrpc.rpc.ResponseFuture;
import com.zjj.jrpc.rpc.message.DefaultResponseFuture;
import com.zjj.jrpc.transport.HeartBeatFactory;
import com.zjj.jrpc.transport.TransChannel;
import com.zjj.jrpc.transport.netty.ChannelState;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;


@Slf4j
public class NettyChannel implements TransChannel {

    private final NettyClient nettyClient;
    private final InetSocketAddress remoteAddress;
    private final HeartBeatFactory heartBeatFactory;
    private volatile ChannelState state = ChannelState.UNINITIALIZED;
    private Channel channel;
    private InetSocketAddress localAddress;

    public NettyChannel(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
        this.remoteAddress = nettyClient.getRemoteAddress();
        this.heartBeatFactory = ExtensionLoader.getExtensionLoader(HeartBeatFactory.class).getDefaultExtension();
    }

    @Override
    public Response request(Request request) throws IOException {
        int requestTimeout = getUrl().getMethodParameter(request.getMethodName(), request.getParameterSign(),
                JRpcURLParamType.REQUEST_TIMEOUT.getName(), JRpcURLParamType.REQUEST_TIMEOUT.getIntValue());
        if (requestTimeout <= 0) {
            throw new JRpcFrameworkException("method request timeout cannot less than or equal zero.", JRpcErrorMessage.FRAMEWORK_INIT_ERROR);
        }
        ResponseFuture<?> responseFuture = new DefaultResponseFuture<>(getUrl(), requestTimeout, request);
        log.info("{} create responseFuture for request {}", this.getClass().getSimpleName(), request);
        nettyClient.registerCallback(request.getRequestId(), responseFuture);
        ChannelFuture writeFuture = channel.writeAndFlush(request).addListener(f -> {
            if (f.isSuccess()) {
                log.info("{} send request {} success.", this.getClass().getSimpleName(), request);
            } else {
                log.error("send request failure, channel: {}", channel);
            }
        });
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
        if (nettyClient.containsCallback(request.getRequestId())) {
            nettyClient.removeCallback(request.getRequestId()).cancel();
        }
        log.error("NettyChannel request [{}] error, url: [{}]", request, getUrl());
        throw new JRpcServiceProviderException(responseFuture.getException());
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
            log.info("NettyChannel {} has started success.", channel);
            state = ChannelState.ACTIVE;
            Request heartbeat = heartBeatFactory.createRequest();
            channel.writeAndFlush(heartbeat).await().addListener(f -> {
                if (f.isSuccess()) {
                    log.info("heartbeat {} send success.", heartbeat);
                }
            });
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
