package com.zjj.transport.netty.server;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.executor.StandardThreadPoolExecutor;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import com.zjj.transport.MessageHandler;
import com.zjj.transport.netty.NettyChannelHandler;
import com.zjj.transport.support.AbstractServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class NettyServer extends AbstractServer {

    private final AtomicInteger rejectConnections = new AtomicInteger(0);

    private NettyServerChannelManager channelManager;
    private NettyChannelHandler handler;
    private StandardThreadPoolExecutor executor;

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private Channel channel;

    private final MessageHandler messageHandler;

    protected NettyServer(JRpcURL url, MessageHandler messageHandler) {
        super(url);
        this.messageHandler = messageHandler;
    }

    public int incrementReject() {
        return rejectConnections.incrementAndGet();
    }

    @Override
    public boolean isBound() {
        return channel != null && channel.isActive();
    }

    @Override
    public Response request(Request request) throws IOException {
        throw new IllegalStateException("NettyServer method request(Request) unsupported.");
    }

    @Override
    public boolean open() {
        if (isAvailable()) {
            log.warn("NettyServer has already open.");
            return true;
        }
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        // 服务端最大连接数
        int maxServerConnection = url.getParameter(JRpcURLParamType.maxServerConnection.getName(),
                JRpcURLParamType.maxServerConnection.getIntValue());
        channelManager = new NettyServerChannelManager(maxServerConnection);

        int corePoolSize = url.getParameter(JRpcURLParamType.corePoolSize.getName(),
                JRpcURLParamType.corePoolSize.getIntValue());
        int maximumPoolSize = url.getParameter(JRpcURLParamType.maximumPoolSize.getName(),
                JRpcURLParamType.maximumPoolSize.getIntValue());
        int workerQueueSize = url.getParameter(JRpcURLParamType.workerQueueSize.getName(),
                JRpcURLParamType.workerQueueSize.getIntValue());

        executor = new StandardThreadPoolExecutor(corePoolSize,
                maximumPoolSize,
                workerQueueSize,
                new DefaultThreadFactory("NettyServer-" + url.getAddress(), true));

        handler = new NettyChannelHandler(this, messageHandler, executor);

        log.info("NettyServer start open URL {}", url);
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<NioServerSocketChannel>() {
                    @Override
                    protected void initChannel(NioServerSocketChannel ch) throws Exception {
                        ch.pipeline().addLast("server-channel-manager", channelManager)
                                .addLast("server-handler", handler);
                    }
                });
        channel = bootstrap.bind(url.getPort()).syncUninterruptibly().channel();
        setLocalAddress((InetSocketAddress) channel.localAddress());
        log.info("NettyServer channel {} is open success with URL {}", channel, url);
        return channel.isActive();
    }

    @Override
    public void close() {
        close(0);
    }

    @Override
    public void close(int timeout) {
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
    }

    @Override
    public boolean isClosed() {
        return !channel.isOpen();
    }

    @Override
    public boolean isAvailable() {
        return isBound();
    }

    @Override
    public JRpcURL getUrl() {
        return url;
    }

    public static void main(String[] args) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("address", "39.105.65.104:2181");
        parameters.put(JRpcURLParamType.registryRetryPeriod.getName(), "1000");
        JRpcURL jRpcURL = new JRpcURL("jrpc", "127.0.0.1", 20855, "com.zjj.registry.zookeeper", parameters);

        NettyServer nettyServer = new NettyServer(jRpcURL, null);
        nettyServer.open();
    }
}
