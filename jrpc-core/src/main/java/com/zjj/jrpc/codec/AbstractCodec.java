package com.zjj.jrpc.codec;

import com.zjj.jrpc.serialize.Serialization;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractCodec implements Codec {
    protected Serialization serialization;

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }

}
