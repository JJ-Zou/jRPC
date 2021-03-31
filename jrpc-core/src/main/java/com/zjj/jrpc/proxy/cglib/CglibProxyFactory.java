package com.zjj.jrpc.proxy.cglib;

import com.zjj.jrpc.clutter.Clutter;
import com.zjj.jrpc.proxy.ProxyFactory;
import net.sf.cglib.proxy.Enhancer;

import java.util.List;

public class CglibProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(Class<T> clazz, List<Clutter<T>> clutters) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new CglibMethodInterceptor<>(clazz, clutters));
        return (T) enhancer.create();
    }


}
