package com.zjj.jrpc.rpc.support;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.common.utils.ReflectUtils;
import com.zjj.jrpc.rpc.Provider;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractProvider<T> implements Provider<T> {
    protected final Class<T> interfaceClass;
    protected final JRpcURL url;
    private final Map<String, Method> methodMap = new HashMap<>();
    private boolean available;
    private boolean destroyed;

    protected AbstractProvider(Class<T> interfaceClass, JRpcURL url) {
        this.interfaceClass = interfaceClass;
        this.url = url;
        initMethodMap();
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public void init() {
        available = true;
    }

    @Override
    public void destroy() {
        available = false;
        destroyed = true;
    }

    private void initMethodMap() {
        Arrays.stream(interfaceClass.getMethods())
                .forEach(method -> {
                    String methodSign = ReflectUtils.getMethodSign(method);
                    methodMap.put(methodSign, method);
                });
    }

    @Override
    public JRpcURL getUrl() {
        return url;
    }

    @Override
    public String desc() {
        return url.toString();
    }

    @Override
    public Class<T> getInterface() {
        return interfaceClass;
    }

    @Override
    public Method lookupMethod(String methodName, String methodParameterSign) {
        String methodSign = ReflectUtils.getMethodSign(methodName, methodParameterSign);
        return methodMap.get(methodSign);
    }


}
