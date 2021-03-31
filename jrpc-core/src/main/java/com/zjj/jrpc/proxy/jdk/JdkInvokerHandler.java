package com.zjj.jrpc.proxy.jdk;

import com.zjj.jrpc.clutter.Clutter;
import com.zjj.jrpc.proxy.support.AbstractInvokerHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class JdkInvokerHandler<T> extends AbstractInvokerHandler<T> implements InvocationHandler {

    public JdkInvokerHandler(Class<T> clazz, List<Clutter<T>> clutters) {
        super(clazz, clazz.getName(), clutters);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return doInvoke(proxy, method, args);
    }
}
