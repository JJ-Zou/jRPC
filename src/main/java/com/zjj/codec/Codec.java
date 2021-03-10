package com.zjj.codec;

public interface Codec {

    byte[] encode(Object message);

    Object decode(byte[] bytes);
}
