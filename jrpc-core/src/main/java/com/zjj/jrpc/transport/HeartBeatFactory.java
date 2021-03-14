package com.zjj.jrpc.transport;

import com.zjj.jrpc.rpc.Request;

public interface HeartBeatFactory {
    Request createRequest();

    MessageHandler wrap(MessageHandler handler);
}
