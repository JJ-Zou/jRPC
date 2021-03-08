package com.zjj.transport.netty;

import com.zjj.codec.Codec;
import com.zjj.common.JRpcURLParamType;
import com.zjj.exception.JRpcErrorMessage;
import com.zjj.exception.JRpcFrameworkException;
import com.zjj.extension.ExtensionLoader;
import com.zjj.protocol.ProtocolVersion;
import com.zjj.rpc.Message;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

    private final Codec codec;

    public NettyCodec() {
        this.codec = ExtensionLoader.getExtensionLoader(Codec.class).getDefaultExtension();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        byte[] data;
        if (msg instanceof Request) {
            data = codec.encode(msg);
        } else if (msg instanceof Response) {
            data = codec.encode(msg);
        } else {
            throw new JRpcFrameworkException("encode " + msg + " error, message type " + msg.getClass() + " not supported.",
                    JRpcErrorMessage.FRAMEWORK_ENCODE_ERROR);
        }
        log.info("NettyEncoder encode {} success by codec {}", msg, codec.getClass().getSimpleName());
        int nettyHeader = ProtocolVersion.NETTY_VERSION.getHeadLength();
        ByteBuf byteBuf = Unpooled.buffer(nettyHeader);
        byteBuf.writeShort(JRpcURLParamType.nettyMagicNum.getShortValue());
        byteBuf.writeZero(10);
        int dataLen = data.length;
        byteBuf.writeInt(dataLen);
        byte[] bytes = new byte[nettyHeader + dataLen];
        byteBuf.readBytes(bytes, 0, nettyHeader);
        System.arraycopy(data, 0, bytes, nettyHeader, dataLen);
        out.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
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
            message = codec.decode(data);
        } catch (Exception e) {
            log.error("{} decode {} fail", this.getClass().getSimpleName(), data);
            return;
        }
        log.info("NettyEncoder decode success message: {}, type: {}", message, message.getClass());
        out.add(message);
    }

}
