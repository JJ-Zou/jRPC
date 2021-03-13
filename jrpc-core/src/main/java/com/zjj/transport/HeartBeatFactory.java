package com.zjj.transport;

import com.zjj.rpc.Request;

public interface HeartBeatFactory {
    Request createRequest();

    MessageHandler wrap(MessageHandler handler);
}
