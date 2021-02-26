package com.zjj.rpc.remoting.transport.netty;

import com.zjj.rpc.remoting.transport.AbstractServer;
import com.zjj.rpc.remoting.transport.netty.NettyServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collection;

@Slf4j
public class NettyServer extends AbstractServer {
    private ServerBootstrap bootstrap;
    private Channel channel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer() {
        super();
    }

    @Override
    protected void doOpen() {
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
        final NettyServerHandler nettyHandler = new NettyServerHandler();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("nettyHandler", nettyHandler);
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(getBindAddress()).syncUninterruptibly();
        channel = channelFuture.channel();
    }

    @Override
    public boolean isBound() {
        return channel.isActive();
    }

    @Override
    public Collection<Channel> getChannels() {
        return null;
    }

    @Override
    public Channel getChannel(InetSocketAddress remotingAddress) {
        return null;
    }


}
