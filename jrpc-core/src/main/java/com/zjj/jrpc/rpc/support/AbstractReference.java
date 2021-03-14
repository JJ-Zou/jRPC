package com.zjj.jrpc.rpc.support;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.rpc.Reference;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.rpc.Response;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractReference<T> extends AbstractNode implements Reference<T> {

    protected final AtomicInteger activeReferCount = new AtomicInteger(0);
    protected final Class<T> clazz;
    protected final JRpcURL serviceUrl;

    protected AbstractReference(Class<T> clazz, JRpcURL url) {
        super(url);
        this.clazz = clazz;
        this.serviceUrl = url;
    }

    protected AbstractReference(Class<T> clazz, JRpcURL url, JRpcURL serviceUrl) {
        super(url);
        this.clazz = clazz;
        this.serviceUrl = serviceUrl;
    }

    @Override
    public Class<T> getInterface() {
        return clazz;
    }

    @Override
    public int activeReferCount() {
        return activeReferCount.get();
    }

    protected void incrActiveReferCount() {
        activeReferCount.incrementAndGet();
    }

    protected void decrActiveReferCount(Request request, Response response) {
        activeReferCount.decrementAndGet();
    }

    public JRpcURL getServiceUrl() {
        return serviceUrl;
    }

    @Override
    public String desc() {
        return "[" + this.getClass().getSimpleName() + "] url=" + url;
    }

    @Override
    public Response call(Request request) {
        if (!isAvailable()) {
            throw new IllegalStateException();
        }
        incrActiveReferCount();
        Response response = null;
        try {
            response = doCall(request);
            return response;
        } finally {
            decrActiveReferCount(request, response);
        }
    }

    protected abstract Response doCall(Request request);
}
