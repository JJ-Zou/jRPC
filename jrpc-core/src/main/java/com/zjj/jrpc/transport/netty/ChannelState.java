package com.zjj.jrpc.transport.netty;

public enum ChannelState {
    UNINITIALIZED(0),
    INITIALIZED(1),
    ACTIVE(2),
    INACTIVE(3),
    CLOSED(4);

    private final int value;

    ChannelState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isInitialized() {
        return this == INITIALIZED;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }
}
