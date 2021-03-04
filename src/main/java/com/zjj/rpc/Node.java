package com.zjj.rpc;

import com.zjj.common.JRpcURL;

public interface Node {
    void init();

    void destroy();

    boolean isAvailable();

    String desc();

    JRpcURL getUrl();
}
