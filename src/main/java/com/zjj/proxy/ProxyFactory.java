package com.zjj.proxy;

import com.zjj.clutter.Clutter;

import java.util.List;

public interface ProxyFactory {
    <T> T getProxy(Class<T> clazz, List<Clutter<T>> clutters);
}
