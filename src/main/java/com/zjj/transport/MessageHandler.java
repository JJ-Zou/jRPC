package com.zjj.transport;

public interface MessageHandler {
    Object handler(TransChannel transChannel, Object message);
}
