package com.zjj.common;

public interface Node {
    URL getUrl();

    boolean isAvailable();

    void destroy();
}
