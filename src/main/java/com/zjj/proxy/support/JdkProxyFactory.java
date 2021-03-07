package com.zjj.proxy.support;

import com.zjj.clutter.Clutter;
import com.zjj.proxy.ProxyFactory;

import java.lang.reflect.Proxy;
import java.util.List;

public class JdkProxyFactory implements ProxyFactory {
    @Override
    public <T> T getProxy(Class<T> clazz, List<Clutter<T>> clutters) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), new InvokerHandler<>(clutters, clazz));
    }
}
