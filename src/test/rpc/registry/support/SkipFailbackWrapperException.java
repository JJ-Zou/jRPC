package com.zjj.rpc.registry.support;

public class SkipFailbackWrapperException extends RuntimeException {
    public SkipFailbackWrapperException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
