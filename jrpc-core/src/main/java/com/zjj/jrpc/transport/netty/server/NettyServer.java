package com.zjj.jrpc.transport.netty.server;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.exception.JRpcFrameworkException;
import com.zjj.jrpc.executor.StandardThreadPoolExecutor;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.rpc.Response;
import com.zjj.jrpc.transport.MessageHandler;
import com.zjj.jrpc.transport.netty.ChannelState;
import com.zjj.jrpc.transport.netty.NettyChannelHandler;
import com.zjj.jrpc.transport.netty.NettyCodec;
import com.zjj.jrpc.transport.support.AbstractServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class NettyServer extends AbstractServer {

    private final AtomicInteger rejectConnections = new AtomicInteger(0);
    private final MessageHandler messageHandler;
    private volatile ChannelState state = ChannelState.UNINITIALIZED;
    private NettyServerChannelManager channelManager;
    private ChannelHandler handler;
    private StandardThreadPoolExecutor executor;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    protected NettyServer(JRpcURL url, MessageHandler messageHandler) {
        super(url);
        this.messageHandler = messageHandler;
    }

    public void incrementReject() {
        rejectConnections.incrementAndGet();
    }


    @Override
    public synchronized boolean open() {
        if (isAvailable()) {
            log.warn("NettyServer has already open.");
            return true;
        }
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        // 服务端最大连接数
        int maxServerConnection = url.getParameter(JRpcURLParamType.MAX_SERVER_CONNECTION.getName(),
                JRpcURLParamType.MAX_SERVER_CONNECTION.getIntValue());
        channelManager = new NettyServerChannelManager(maxServerConnection);

        int corePoolSize = url.getParameter(JRpcURLParamType.CORE_POOL_SIZE.getName(),
                JRpcURLParamType.CORE_POOL_SIZE.getIntValue());
        int maximumPoolSize = url.getParameter(JRpcURLParamType.MAXIMUM_POOL_SIZE.getName(),
                JRpcURLParamType.MAXIMUM_POOL_SIZE.getIntValue());
        int workerQueueSize = url.getParameter(JRpcURLParamType.WORKER_QUEUE_SIZE.getName(),
                JRpcURLParamType.WORKER_QUEUE_SIZE.getIntValue());

        executor = new StandardThreadPoolExecutor(corePoolSize,
                maximumPoolSize,
                workerQueueSize,
                new DefaultThreadFactory("NettyServer-" + url.getPort(), true));

        handler = new NettyChannelHandler(this, messageHandler, executor);

        log.info("NettyServer start open port {}", url.getPort());
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("client-codec", new NettyCodec())
                                .addLast("server-channel-manager", channelManager)
                                .addLast("server-handler", handler);
                    }
                });
        channel = bootstrap.bind(url.getPort()).syncUninterruptibly().channel();
        setLocalAddress((InetSocketAddress) channel.localAddress());
        state = ChannelState.ACTIVE;
        log.info("NettyServer channel {} is open success with URL {}", channel, url);
        return state.isActive();
    }

    @Override
    public boolean isBound() {
        return channel != null && channel.isActive();
    }

    @Override
    public Response request(Request request) throws IOException {
        throw new JRpcFrameworkException("NettyServer method request(Request) unsupported.");
    }

    @Override
    public void close() {
        if (isClosed()) {
            return;
        }
        if (channel != null) {
            channel.close();
        }
        if (channelManager != null) {
            channelManager.closeAll();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
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
        return url;
    }
}
