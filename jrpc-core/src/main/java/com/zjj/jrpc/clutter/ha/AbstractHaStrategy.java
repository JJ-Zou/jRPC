package com.zjj.jrpc.clutter.ha;

import com.zjj.jrpc.clutter.HaStrategy;
import com.zjj.jrpc.common.JRpcURL;

public abstract class AbstractHaStrategy<T> implements HaStrategy<T> {
    protected JRpcURL url;

    @Override
    public void setUrl(JRpcURL url) {
        this.url = url;
    }
}
