package com.zjj.transport.netty.client;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import com.zjj.transport.TransChannel;
import com.zjj.transport.netty.ChannelState;
import com.zjj.transport.support.AbstractClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class NettyClient extends AbstractClient {

    private Bootstrap bootstrap;
    private NioEventLoopGroup eventLoopGroup;

    private volatile ChannelState state = ChannelState.UNINITIALIZED;

    protected NettyClient(JRpcURL url) {
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
            int connectTimeoutMills = getUrl().getParameter(JRpcURLParamType.connectTimeoutMills.getName(), JRpcURLParamType.connectTimeoutMills.getIntValue());
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMills)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast();
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
            throw new IllegalStateException("NettyClient is unavailable.");
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
        return response;
    }


    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        closeAll();
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

    public static void main(String[] args) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("address", "39.105.65.104:2181");
        parameters.put(JRpcURLParamType.registryRetryPeriod.getName(), "1000");
        JRpcURL jRpcURL = new JRpcURL("jrpc", "127.0.0.1", 20855, "com.zjj.registry.zookeeper", parameters);

        NettyClient nettyClient = new NettyClient(jRpcURL);
        nettyClient.open();
    }
}
