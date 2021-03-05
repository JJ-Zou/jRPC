package com.zjj.transport.netty;

import com.zjj.codec.Codec;
import com.zjj.rpc.Response;
import com.zjj.transport.MessageHandler;
import com.zjj.transport.TransChannel;
import com.zjj.transport.netty.server.NettyServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class NettyChannelHandler extends ChannelDuplexHandler {

    private final ThreadPoolExecutor executor;
    private final TransChannel transChannel;
    private final MessageHandler messageHandler;
    private final Codec codec;

    public NettyChannelHandler(TransChannel transChannel, MessageHandler messageHandler) {
        this(transChannel, messageHandler, null);
    }

    public NettyChannelHandler(TransChannel transChannel, MessageHandler messageHandler, ThreadPoolExecutor executor) {
        this.transChannel = transChannel;
        this.messageHandler = messageHandler;
        this.executor = executor;
        this.codec = ServiceLoader.load(Codec.class).iterator().next();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        if (!(msg instanceof NettyMessage)) {
            log.error("NettyChannelHandler channel {} read type not support: {}.", channel, msg.getClass());
            throw new IllegalArgumentException("NettyChannelHandler channel " + channel + " read type not support: " + msg.getClass());
        }
        NettyMessage nettyMessage = (NettyMessage) msg;
        if (executor == null) {
            processNettyMessage(channel, nettyMessage);
            log.info("Unavailable thread pool, run task in netty thread {}.", Thread.currentThread());
            return;
        }
        try {
            executor.execute(() -> processNettyMessage(channel, nettyMessage));
            log.info("Run task in thread pool {}.", executor);
        } catch (RejectedExecutionException e) {
            if (nettyMessage.isRequest()) {
                // 拒绝请求消息
                Response response = null;// todo:
                sendResponse(channel, response);
                log.error("{} is full, reject this task and send response {} to peer.", executor, response, e);
                if (transChannel instanceof NettyServer) {
                    ((NettyServer) transChannel).incrementReject();
                }
            } else {
                processNettyMessage(channel, nettyMessage);
                log.warn("{} is full, run task in netty thread {}.", executor, Thread.currentThread());
            }
        }
    }

    private void processNettyMessage(Channel channel, NettyMessage nettyMessage) {

    }

    private void sendResponse(Channel channel, Response response) {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("NettyServerHandler channelActive channel = [{}]", ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("NettyServerHandler channelInactive channel = [{}]", ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("NettyServerHandler exceptionCaught channel = [{}], we will close this channel.", ctx.channel(), cause);
        ctx.channel().close();
    }
}
