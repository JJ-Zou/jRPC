package com.zjj.jrpc.transport;

import com.zjj.jrpc.rpc.Request;

public interface Client extends Endpoint {
    void heartbeat(Request request);
}
