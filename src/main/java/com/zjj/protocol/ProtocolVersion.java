package com.zjj.protocol;

public enum ProtocolVersion {
    DEFAULT_VERSION((byte) 1, 16),
    NETTY_VERSION((byte) 2, 16),
    ;

    private final byte version;
    private final int headLength;

    ProtocolVersion(byte version, int headLength) {
        this.version = version;
        this.headLength = headLength;
    }

    public byte getVersion() {
        return version;
    }

    public int getHeadLength() {
        return headLength;
    }
}
