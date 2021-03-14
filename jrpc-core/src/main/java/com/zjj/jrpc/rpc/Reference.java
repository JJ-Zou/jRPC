package com.zjj.jrpc.rpc;

import com.zjj.jrpc.common.JRpcURL;

public interface Reference<T> extends Caller<T> {
    int activeReferCount();

    JRpcURL getUrl();
}
