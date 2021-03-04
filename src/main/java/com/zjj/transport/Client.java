package com.zjj.transport;

import com.zjj.rpc.Request;

public interface Client extends EndPoint {
    void heartbeat(Request request);
}
