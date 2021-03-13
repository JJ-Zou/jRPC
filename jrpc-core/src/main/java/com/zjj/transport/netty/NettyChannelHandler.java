package com.zjj.transport.netty;

import com.zjj.common.JRpcURLParamType;
import com.zjj.exception.JRpcErrorMessage;
import com.zjj.exception.JRpcFrameworkException;
import com.zjj.exception.JRpcServiceProviderException;
import com.zjj.rpc.Message;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import com.zjj.rpc.context.RpcContext;
import com.zjj.rpc.message.DefaultResponse;
import com.zjj.transport.MessageHandler;
import com.zjj.transport.TransChannel;
import com.zjj.transport.netty.server.NettyServer;
import com.zjj.transport.support.DefaultHeartBeatFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@ChannelHandler.Sharable
public class NettyChannelHandler extends ChannelDuplexHandler {

    private final ThreadPoolExecutor executor;
    private final TransChannel transChannel;
    private final MessageHandler messageHandler;

    public NettyChannelHandler(TransChannel transChannel, MessageHandler messageHandler) {
        this(transChannel, messageHandler, null);
    }

    public NettyChannelHandler(TransChannel transChannel, MessageHandler messageHandler, ThreadPoolExecutor executor) {
        this.transChannel = transChannel;
        this.messageHandler = messageHandler;
        this.executor = executor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        if (!(msg instanceof Message)) {
            log.error("NettyChannelHandler channel {} read type not support: {}.", channel, msg.getClass());
            throw new JRpcFrameworkException("NettyChannelHandler channel " + channel + " read type not support: " + msg.getClass(), JRpcErrorMessage.FRAMEWORK_DECODE_ERROR);
        }
        Message message = (Message) msg;
        if (executor == null) {
            log.info("Unavailable thread pool, run task in netty thread {}.", Thread.currentThread());
            processMessage(channel, message);
            return;
        }
        try {
            executor.execute(() -> {
                log.info("Run task in thread pool {}.", executor);
                processMessage(channel, message);
            });
        } catch (RejectedExecutionException e) {
            if (message instanceof Request) {
                Request request = (Request) message;
                // 拒绝请求消息
                Response response = DefaultResponse.builder()
                        .requestId(request.getRequestId())
                        .protocolVersion(request.getProtocolVersion())
                        .value(new JRpcServiceProviderException("thread poll is full, request reject by server: " + channel.localAddress(), JRpcErrorMessage.SERVICE_REJECT_ERROR))
                        .build();
                channel.writeAndFlush(response).addListener(future -> {
                    if (future.isSuccess()) {
                        log.error("{} is full, reject this task and send response {} to peer.", executor, response, e);
                    } else {
                        log.error("{}: channel {} is not available, message send fail.", this.getClass().getSimpleName(), channel);
                    }
                });
                if (transChannel instanceof NettyServer) {
                    ((NettyServer) transChannel).incrementReject();
                }
            } else {
                log.warn("{} is full, run task in netty thread {}.", executor, Thread.currentThread());
                processResponse((Response) message);
            }
        }
    }

    private void processMessage(Channel channel, Message message) {
        if (message instanceof Request) {
            processRequest(channel, (Request) message);
        } else {
            processResponse((Response) message);
        }
    }

    private void processRequest(Channel channel, Request request) {
        if (DefaultHeartBeatFactory.HeartBeatRequest.class.getName().equals(request.getInterfaceName())) {
            log.info("receive heart from: {}", channel);
            return;
        }
        request.setAttachment(JRpcURLParamType.HOST.getName(), ((InetSocketAddress) channel.remoteAddress()).getHostName());
        long startTime = System.currentTimeMillis();
        try {
            RpcContext.init(request);
            Object result = messageHandler.handler(transChannel, request);
            final DefaultResponse response;
            if (result instanceof DefaultResponse) {
                response = (DefaultResponse) result;
            } else {
                response = new DefaultResponse(result);
            }
            response.setRequestId(request.getRequestId());
            response.setProcessTime(System.currentTimeMillis() - startTime);
            channel.writeAndFlush(response).addListener(future -> {
                if (future.isSuccess()) {
                    log.info("{} process request {} success and response {}, process time = {} ms", this.getClass().getSimpleName(), request, response, response.getProcessTime());
                }
            });
        } finally {
            RpcContext.destroy();
        }
    }

    private void processResponse(Response response) {
        log.info("{} processResponse {}", this.getClass().getSimpleName(), response);
        messageHandler.handler(transChannel, response);
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
