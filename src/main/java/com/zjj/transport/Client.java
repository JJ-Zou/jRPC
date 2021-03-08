package com.zjj.transport;

import com.zjj.rpc.Request;

public interface Client extends Endpoint {
    void heartbeat(Request request);
}
