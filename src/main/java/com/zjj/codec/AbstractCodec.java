package com.zjj.codec;

import com.zjj.serialize.Serialization;

public abstract class AbstractCodec implements Codec {
    protected Serialization serialization;

    public void setSerialization(Serialization serialization) {
        this.serialization = serialization;
    }
}
