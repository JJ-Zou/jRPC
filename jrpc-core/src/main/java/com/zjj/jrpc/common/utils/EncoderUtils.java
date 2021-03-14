package com.zjj.jrpc.common.utils;

import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.protocol.ProtocolVersion;

public class EncoderUtils {
    private EncoderUtils() {
    }

    public static byte[] wrapperHeader(byte[] body, short magic, byte flag, long requestId) {
        int headLen;
        if (magic == JRpcURLParamType.MAGIC_NUM.getShortValue()) {
            headLen = ProtocolVersion.DEFAULT_VERSION.getHeadLength();
        } else if (magic == JRpcURLParamType.NETTY_MAGIC_NUM.getShortValue()) {
            headLen = ProtocolVersion.NETTY_VERSION.getHeadLength();
        } else {
            headLen = 0;
        }
        int bodyLen = body.length;
        byte[] data = new byte[headLen + bodyLen];
        int offset = 0;
        data[offset++] = (byte) (magic >> 8);
        data[offset++] = (byte) magic;
        byte version = ProtocolVersion.DEFAULT_VERSION.getVersion();
        data[offset++] = version;
        data[offset++] = flag;
        data[offset++] = (byte) (requestId >> 56);
        data[offset++] = (byte) (requestId >> 48);
        data[offset++] = (byte) (requestId >> 40);
        data[offset++] = (byte) (requestId >> 32);
        data[offset++] = (byte) (requestId >> 24);
        data[offset++] = (byte) (requestId >> 16);
        data[offset++] = (byte) (requestId >> 8);
        data[offset++] = (byte) requestId;
        data[offset++] = (byte) (bodyLen >> 24);
        data[offset++] = (byte) (bodyLen >> 16);
        data[offset++] = (byte) (bodyLen >> 8);
        data[offset++] = (byte) bodyLen;
        System.arraycopy(body, 0, data, offset, bodyLen);
        return data;
    }
}
