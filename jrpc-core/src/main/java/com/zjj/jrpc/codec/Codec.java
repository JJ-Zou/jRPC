package com.zjj.jrpc.codec;

public interface Codec {

    byte[] encode(Object message);

    Object decode(byte[] bytes);
}
