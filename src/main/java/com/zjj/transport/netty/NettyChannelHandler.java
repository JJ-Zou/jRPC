package com.zjj.transport.netty;

import com.zjj.codec.Codec;
import com.zjj.common.JRpcURLParamType;
import com.zjj.exception.JRpcErrorMessage;
import com.zjj.exception.JRpcFrameworkException;
import com.zjj.exception.JRpcServiceProviderException;
import com.zjj.extension.ExtensionLoader;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import com.zjj.rpc.context.RpcContext;
import com.zjj.rpc.message.DefaultResponse;
import com.zjj.transport.MessageHandler;
import com.zjj.transport.TransChannel;
import com.zjj.transport.netty.server.NettyServer;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@ChannelHandler.Sharable
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
        this.codec = ExtensionLoader.getExtensionLoader(Codec.class).getDefaultExtension();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        if (!(msg instanceof NettyMessage)) {
            log.error("NettyChannelHandler channel {} read type not support: {}.", channel, msg.getClass());
            throw new JRpcFrameworkException("NettyChannelHandler channel " + channel + " read type not support: " + msg.getClass(), JRpcErrorMessage.FRAMEWORK_DECODE_ERROR);
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
                Response response = DefaultResponse.builder()
                        .requestId(nettyMessage.getRequestId())
                        .protocolVersion(nettyMessage.getVersion().getVersion())
                        .value(new JRpcServiceProviderException("thread poll is full, request reject by server: " + channel.localAddress(), JRpcErrorMessage.SERVICE_REJECT_ERROR))
                        .build();
                sendResponse(channel, response).addListener(future -> {
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
                processNettyResponse(nettyMessage);
                log.warn("{} is full, run task in netty thread {}.", executor, Thread.currentThread());
            }
        }
    }

    private void processNettyMessage(Channel channel, NettyMessage nettyMessage) {
        Object result;
        try {
            result = codec.decode(transChannel, nettyMessage.getData());
        } catch (IOException e) {
            log.error("{} decode fail, requestId = {}", this.getClass().getSimpleName(), nettyMessage.getRequestId());
            DefaultResponse response = DefaultResponse.builder()
                    .requestId(nettyMessage.getRequestId())
                    .protocolVersion(nettyMessage.getVersion().getVersion())
                    .exception(e)
                    .build();
            if (nettyMessage.isRequest()) {
                sendResponse(channel, response);
            } else {
                processResponse(response);
            }
            return;
        }
        if (nettyMessage.isRequest()) {
            processRequest(channel, (Request) result);
        } else {
            processResponse(result);
        }
    }

    private void processNettyResponse(NettyMessage nettyMessage) {
        Response response;
        try {
            response = (Response) codec.decode(transChannel, nettyMessage.getData());
        } catch (IOException e) {
            log.error("{} decode fail, requestId = {}", this.getClass().getSimpleName(), nettyMessage.getRequestId());
            DefaultResponse defaultResponse = DefaultResponse.builder()
                    .requestId(nettyMessage.getRequestId())
                    .protocolVersion(nettyMessage.getVersion().getVersion())
                    .exception(e)
                    .build();
            processResponse(defaultResponse);
            return;
        }
        processResponse(response);
    }

    private void processRequest(Channel channel, Request request) {
        request.setAttachment(JRpcURLParamType.host.getName(), ((InetSocketAddress) channel.remoteAddress()).getHostName());
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
            sendResponse(channel, response).addListener(future -> {
                if (future.isSuccess()) {
                    log.info("{} process request {} success and response {}, process time = {} ms", this.getClass().getSimpleName(), request, response, response.getProcessTime());
                }
            });
        } finally {
            RpcContext.destroy();
        }
    }

    private void processResponse(Object response) {
        messageHandler.handler(transChannel, response);
    }

    private ChannelFuture sendResponse(Channel channel, Response response) {
        byte[] data = null;
        try {
            data = codec.encode(transChannel, response);
        } catch (IOException e) {
            log.error("NettyChannelHandler: {} encode {} fail", codec, response);
        }
        return channel.writeAndFlush(data);
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
