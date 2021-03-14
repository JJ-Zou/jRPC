package com.zjj.jrpc.proxy;

import com.zjj.jrpc.clutter.Clutter;

import java.util.List;

public interface ProxyFactory {
    <T> T getProxy(Class<T> clazz, List<Clutter<T>> clutters);
}
