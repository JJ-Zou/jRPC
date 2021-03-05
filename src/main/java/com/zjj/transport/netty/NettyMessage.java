package com.zjj.transport.netty;

import com.zjj.rpc.ProtocolVersion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NettyMessage {
    private boolean isRequest;
    private long requestId;
    private byte[] data;
    private long startTime;
    private ProtocolVersion version;
}
