package com.zjj.clutter.ha;

import com.zjj.clutter.HaStrategy;
import com.zjj.common.JRpcURL;

public abstract class AbstractHaStrategy<T> implements HaStrategy<T> {
    protected JRpcURL url;

    @Override
    public void setUrl(JRpcURL url) {
        this.url = url;
    }
}
