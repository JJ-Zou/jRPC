package com.zjj.rpc;

import java.lang.reflect.Method;

public interface Provider<T> extends Caller<T> {
    Method lookupMethod(String methodName, String methodParameterSign);

    T getImpl();
}
