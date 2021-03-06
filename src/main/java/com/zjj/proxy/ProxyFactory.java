package com.zjj.proxy;

public interface ProxyFactory {
    <T> T getProxy(Class<T> clazz);
}
