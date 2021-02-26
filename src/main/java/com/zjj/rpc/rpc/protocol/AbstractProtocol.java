package com.zjj.rpc.rpc.protocol;

import com.zjj.rpc.rpc.Protocol;
import com.zjj.rpc.rpc.ProtocolServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractProtocol implements Protocol {
    protected final Map<String, ProtocolServer> serverMap = new ConcurrentHashMap<>();
}
