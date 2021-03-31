package com.zjj.jrpc.proxy;

import com.zjj.jrpc.clutter.Clutter;
import com.zjj.jrpc.extension.SPI;

import java.util.List;

@SPI("cglib")
public interface ProxyFactory {
    <T> T getProxy(Class<T> clazz, List<Clutter<T>> clutters);
}
