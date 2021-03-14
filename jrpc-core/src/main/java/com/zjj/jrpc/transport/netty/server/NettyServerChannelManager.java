package com.zjj.jrpc.transport.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@ChannelHandler.Sharable
public class NettyServerChannelManager extends ChannelInboundHandlerAdapter {

    private static final ConcurrentMap<String, Channel> CHANNELS = new ConcurrentHashMap<>();

    private final int maxServerConnection;

    public NettyServerChannelManager(int maxServerConnection) {
        this.maxServerConnection = maxServerConnection;
    }

    private String channelKey(Channel channel) {
        InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
        InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
        StringBuilder builder = new StringBuilder();
        if (localAddress == null) {
            builder.append(localAddress);
        } else {
            builder.append(localAddress.getHostName())
                    .append(":")
                    .append(localAddress.getPort());
        }
        builder.append("-");

        if (remoteAddress == null) {
            builder.append(remoteAddress);
        } else {
            builder.append(remoteAddress.getHostName())
                    .append(":")
                    .append(remoteAddress.getPort());
        }
        return builder.toString();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        if (CHANNELS.size() >= maxServerConnection) {
            log.warn("Connection size {} out of limit {}, close current channel {}", CHANNELS.size(), maxServerConnection, channel);
            channel.close();
            return;
        }
        String channelKey = channelKey(channel);
        CHANNELS.put(channelKey, channel);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String channelKey = channelKey(channel);
        CHANNELS.remove(channelKey);
        super.channelUnregistered(ctx);
    }

    public void closeAll() {
        CHANNELS.values().forEach(channel -> {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (Exception e) {
                log.error("NettyServerChannelManager close channel error", e);
            }
        });
    }
}
