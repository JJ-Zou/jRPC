package com.zjj.jrpc.rpc.support;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.exception.JRpcErrorMessage;
import com.zjj.jrpc.exception.JRpcFrameworkException;
import com.zjj.jrpc.rpc.Node;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public abstract class AbstractNode implements Node {
    protected final AtomicBoolean init = new AtomicBoolean(false);
    protected final AtomicBoolean available = new AtomicBoolean(false);

    protected final JRpcURL url;

    protected AbstractNode(JRpcURL url) {
        this.url = url;
    }

    @Override
    public void init() {
        if (init.get()) {
            log.warn("{} has already been initialized.", this);
        }
        try {
            boolean success = doInit();
            init.set(success);
            available.set(success);
        } catch (Exception e) {
            log.warn("Exporter {} init fail.", desc());
            throw new JRpcFrameworkException(JRpcErrorMessage.FRAMEWORK_INIT_ERROR);
        }
    }

    @Override
    public boolean isAvailable() {
        return available.get();
    }

    @Override
    public JRpcURL getUrl() {
        return url;
    }

    protected abstract boolean doInit();
}
