package com.zjj.rpc;

import com.zjj.common.JRpcURL;

public interface Reference<T> extends Caller<T> {
    int activeReferCount();

    JRpcURL getUrl();
}
