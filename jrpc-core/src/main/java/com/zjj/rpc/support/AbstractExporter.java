package com.zjj.rpc.support;

import com.zjj.common.JRpcURL;
import com.zjj.rpc.Exporter;
import com.zjj.rpc.Provider;

public abstract class AbstractExporter<T> extends AbstractNode implements Exporter<T> {
    protected final Provider<T> provider;

    protected AbstractExporter(JRpcURL url, Provider<T> provider) {
        super(url);
        this.provider = provider;
    }

    @Override
    public Provider<T> getProvider() {
        return provider;
    }

    @Override
    public String desc() {
        return "[" + this.getClass().getSimpleName() + "] URL = " + getUrl();
    }
}
