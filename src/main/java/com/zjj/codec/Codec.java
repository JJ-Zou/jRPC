package com.zjj.codec;

import com.zjj.transport.TransChannel;

import java.io.IOException;

public interface Codec {

    byte[] encode(TransChannel channel, Object message) throws IOException;

    Object decode(TransChannel channel, byte[] bytes) throws IOException;
}
