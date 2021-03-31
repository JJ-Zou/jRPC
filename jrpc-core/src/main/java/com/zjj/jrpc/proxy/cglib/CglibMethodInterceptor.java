package com.zjj.jrpc.proxy.cglib;

import com.zjj.jrpc.clutter.Clutter;
import com.zjj.jrpc.proxy.support.AbstractInvokerHandler;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

public class CglibMethodInterceptor<T> extends AbstractInvokerHandler<T> implements MethodInterceptor {

    protected CglibMethodInterceptor(Class<T> clazz, List<Clutter<T>> clutters) {
        super(clazz, clazz.getName(), clutters);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        return doInvoke(o, method, args);
    }
}
