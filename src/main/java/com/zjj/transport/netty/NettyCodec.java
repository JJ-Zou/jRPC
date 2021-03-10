package com.zjj.transport.netty;

import com.zjj.codec.Codec;
import com.zjj.common.JRpcURLParamType;
import com.zjj.common.utils.EncoderUtils;
import com.zjj.exception.JRpcErrorMessage;
import com.zjj.exception.JRpcFrameworkException;
import com.zjj.extension.ExtensionLoader;
import com.zjj.protocol.ProtocolVersion;
import com.zjj.rpc.Message;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 通过指定固定长协议头，将数据长度记录在协议中，来解决粘包拆包。
 * FixedLengthFrameDecoder
 */
@Slf4j
public class NettyCodec extends ByteToMessageCodec<Message> {

    private static final Codec CODEC = ExtensionLoader.getExtensionLoader(Codec.class).getDefaultExtension();

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        byte[] data;
        log.info("message {} comes into NettyCodec encoder {}", msg, this);
        if (msg instanceof Request) {
            data = CODEC.encode(msg);
        } else if (msg instanceof Response) {
            data = CODEC.encode(msg);
        } else {
            throw new JRpcFrameworkException("encode " + msg + " error, message type " + msg.getClass() + " not supported.",
                    JRpcErrorMessage.FRAMEWORK_ENCODE_ERROR);
        }
        byte[] bytes = EncoderUtils.wrapperHeader(data, JRpcURLParamType.nettyMagicNum.getShortValue(), (byte) 0, msg.getRequestId());
        log.info("NettyEncoder encode {} success by {}", msg, this);
        out.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        log.info("byteBuf {} comes into NettyCodec decoder {}", byteBuf, this);
        if (byteBuf.readableBytes() < ProtocolVersion.NETTY_VERSION.getHeadLength()) {
            log.info("In this case, maybe the current buffer space is insufficient or there is a TCP packet sticking.byteBuf readable: {}, netty header length {}", byteBuf.readableBytes(), ProtocolVersion.NETTY_VERSION.getHeadLength());
            return;
        }
        byteBuf.markReaderIndex();
        short nettyMagicNum = byteBuf.readShort();
        if (nettyMagicNum != JRpcURLParamType.nettyMagicNum.getShortValue()) {
            log.error("In this case, this package magic num not supported, we will not solve this msg.This could be a forgery package!");
            throw new JRpcFrameworkException("NettyCodec do not support this magic num: " + nettyMagicNum + ".");
        }
        // 暂时未用到的信息
        byteBuf.skipBytes(10);
        int dataLen = byteBuf.readInt();
        if (byteBuf.readableBytes() < dataLen) {
            log.info("In this case, maybe the current buffer space is insufficient or there is a TCP packet sticking. byteBuf readable: {}, dataLen {}", byteBuf.readableBytes(), dataLen);
            byteBuf.resetReaderIndex();
            return;
        }
        Object message;
        byte[] data = new byte[dataLen];
        byteBuf.readBytes(data);
        try {
            message = CODEC.decode(data);
        } catch (Exception e) {
            log.error("{} decode {} fail", this.getClass().getSimpleName(), data);
            return;
        }
        log.info("NettyEncoder decode success message: {}, type: {} by {}", message, message.getClass(), this);
        out.add(message);
    }

}
