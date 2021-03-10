package com.zjj.rpc;


import com.zjj.protocol.ProtocolVersion;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public abstract class ResponseFuture<V> implements JFuture<V>, Response {

    protected static final AtomicReferenceFieldUpdater<ResponseFuture, Object> RESULT_UPDATE
            = AtomicReferenceFieldUpdater.newUpdater(ResponseFuture.class, Object.class, "state");

    protected static final Object DONE = new Object();
    protected static final Object RUNNING = new Object();
    protected static final Object CANCELED = new Object();

    protected volatile Object state = RUNNING;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new IllegalStateException();
    }


    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        throw new IllegalStateException();
    }

    @Override
    public byte getProtocolVersion() {
        return ProtocolVersion.DEFAULT_VERSION.getVersion();
    }

    @Override
    public void setProtocolVersion(byte protocolVersion) {
        throw new IllegalStateException();
    }

    @Override
    public int getSerializeNumber() {
        return 0;
    }

    @Override
    public void setSerializeNumber(int number) {
        throw new IllegalStateException();
    }

    public abstract void onSuccess(Response response);

    public abstract void onFailure(Response response);

    public abstract long getCreateTime();

    public abstract void setReturnType(Class<V> clazz);

}
