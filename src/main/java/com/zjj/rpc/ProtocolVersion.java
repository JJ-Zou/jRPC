package com.zjj.rpc;

public enum ProtocolVersion {
    VERSION_1((byte) 1, 16),
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
