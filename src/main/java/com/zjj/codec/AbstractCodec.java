package com.zjj.codec;

import com.zjj.common.JRpcURLParamType;
import com.zjj.protocol.ProtocolVersion;
import com.zjj.serialize.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class AbstractCodec implements Codec {
    protected Serialization serialization;

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

}
