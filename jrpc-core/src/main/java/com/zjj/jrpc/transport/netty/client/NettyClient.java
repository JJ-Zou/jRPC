package com.zjj.jrpc.transport.netty.client;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.exception.JRpcErrorMessage;
import com.zjj.jrpc.exception.JRpcFrameworkException;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.rpc.Response;
import com.zjj.jrpc.rpc.ResponseFuture;
import com.zjj.jrpc.rpc.message.DefaultResponse;
import com.zjj.jrpc.transport.TransChannel;
import com.zjj.jrpc.transport.netty.ChannelState;
import com.zjj.jrpc.transport.netty.NettyChannelHandler;
import com.zjj.jrpc.transport.netty.NettyCodec;
import com.zjj.jrpc.transport.support.AbstractClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Slf4j
public class NettyClient extends AbstractClient {

    private final ConcurrentMap<Long, ResponseFuture<?>> callbackMap = new ConcurrentHashMap<>();

    private volatile ChannelState state = ChannelState.UNINITIALIZED;
    private Bootstrap bootstrap;
    private NioEventLoopGroup eventLoopGroup;
    private ChannelHandler handler;

    public NettyClient(JRpcURL url) {
        super(url);
    }

    @Override
    public boolean open() {
        if (isAvailable()) {
            log.warn("NettyClient has already open.");
            return true;
        }
        try {
            bootstrap = new Bootstrap();
            eventLoopGroup = new NioEventLoopGroup();
            handler = new NettyChannelHandler(this, (c, o) -> {
                Response response = (Response) o;
                ResponseFuture<?> responseFuture = removeCallback(response.getRequestId());
                if (responseFuture == null) {
                    log.warn("Netty client has response from server but get null, request id = {}", response.getRequestId());
                    return null;
                }
                if (response.getException() == null) {
                    responseFuture.onSuccess(response);
                    log.info("Netty client get success responseFuture process time {}ms", responseFuture.getProcessTime());
                } else {
                    responseFuture.onFailure(response);
                    log.warn("Netty client get failure responseFuture process time {}ms, exception: {}", responseFuture.getProcessTime(), response.getException().getMessage());
                }
                return null;
            });
            int connectTimeoutMills = getUrl().getParameter(JRpcURLParamType.CONNECT_TIMEOUT_MILLS.getName(), JRpcURLParamType.CONNECT_TIMEOUT_MILLS.getIntValue());
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMills)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("client-codec", new NettyCodec())
                                    .addLast("client-handler", handler);
                        }
                    });
            initialize();
            state = ChannelState.ACTIVE;
            log.info("NettyClient with {} started.", getUrl());
        } catch (Exception e) {
            log.info("NettyClient with {} start error.", getUrl(), e);
            state = ChannelState.INACTIVE;
            close();
        }
        return isAvailable();
    }

    protected ChannelFuture connect() {
        return bootstrap.connect(getRemoteAddress());
    }

    @Override
    public void heartbeat(Request request) {
        if (!isAvailable()) {
            return;
        }
        try {
            request(request, true);
            log.info("heartbeat {} send success.", request);
        } catch (Exception e) {
            log.error("heartbeat {} send fail.", request, e);
        }
    }

    @Override
    public Response request(Request request) throws IOException {
        if (!isAvailable()) {
            throw new JRpcFrameworkException("NettyClient is unavailable.", JRpcErrorMessage.FRAMEWORK_INIT_ERROR);
        }

        return request(request, false);
    }

    private Response request(Request request, boolean async) throws IOException {
        TransChannel channel = obtainActiveChannel();
        if (channel == null) {
            throw new IllegalStateException("cannot get active channel.");
        }
        Response response = channel.request(request);
        //todo:
        return new DefaultResponse(response);
    }


    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        closeAll();
        callbackMap.clear();
        eventLoopGroup.shutdownGracefully();
        state = ChannelState.CLOSED;
        log.info("NettyClient closed.");
    }


    @Override
    public boolean isClosed() {
        return state.isClosed();
    }

    @Override
    public boolean isAvailable() {
        return state.isActive();
    }

    public void registerCallback(long requestId, ResponseFuture<?> responseFuture) {
        callbackMap.put(requestId, responseFuture);
    }

    public ResponseFuture removeCallback(long requestId) {
        return callbackMap.remove(requestId);
    }

    public boolean containsCallback(long requestId) {
        return callbackMap.containsKey(requestId);
    }

}
