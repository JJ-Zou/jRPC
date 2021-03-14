package com.zjj.jrpc.transport;

public interface MessageHandler {
    Object handler(TransChannel transChannel, Object message);
}
