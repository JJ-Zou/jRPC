package com.zjj.rpc.common;

public interface Node {
    URL getUrl();

    boolean isAvailable();

    void destroy();
}
