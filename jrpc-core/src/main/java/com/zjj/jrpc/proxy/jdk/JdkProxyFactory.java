package com.zjj.jrpc.proxy.jdk;

import com.zjj.jrpc.clutter.Clutter;
import com.zjj.jrpc.proxy.ProxyFactory;

import java.lang.reflect.Proxy;
import java.util.List;

public class JdkProxyFactory implements ProxyFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProxy(Class<T> clazz, List<Clutter<T>> clutters) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new JdkInvokerHandler<>(clazz, clutters));
    }
}
