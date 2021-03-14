package com.zjj.jrpc.rpc;

import com.zjj.jrpc.common.JRpcURL;

public interface Node {
    void init();

    void destroy();

    boolean isAvailable();

    String desc();

    JRpcURL getUrl();
}
