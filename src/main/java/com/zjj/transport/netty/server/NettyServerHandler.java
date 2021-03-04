package com.zjj.transport.netty.server;

import com.zjj.transport.MessageHandler;
import com.zjj.transport.TransChannel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelDuplexHandler {

    private final ThreadPoolExecutor executor;
    private final TransChannel transChannel;
    private final MessageHandler messageHandler;

    public NettyServerHandler(TransChannel transChannel, MessageHandler messageHandler) {
        this(transChannel, messageHandler, null);
    }

    public NettyServerHandler(TransChannel transChannel, MessageHandler messageHandler, ThreadPoolExecutor executor) {
        this.transChannel = transChannel;
        this.messageHandler = messageHandler;
        this.executor = executor;
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
