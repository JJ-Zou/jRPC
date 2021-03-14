package com.zjj.jrpc.rpc.support;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.exception.JRpcServiceProviderException;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.rpc.Response;
import com.zjj.jrpc.rpc.message.DefaultResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DefaultProvider<T> extends AbstractProvider<T> {

    private final T proxy;

    public DefaultProvider(Class<T> interfaceClass, T proxy, JRpcURL url) {
        super(interfaceClass, url);
        this.proxy = proxy;
    }

    @Override
    public T getImpl() {
        return proxy;
    }

    @Override
    public Response call(Request request) {
        DefaultResponse response = new DefaultResponse();
        Method method = lookupMethod(request.getMethodName(), request.getParameterSign());
        if (method == null) {
            response.setException(new JRpcServiceProviderException());
            return response;
        }
        try {
            Object value = method.invoke(proxy, request.getArguments());
            response.setValue(value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            response.setException(e);
        }
        response.setAttachments(request.getAttachments());
        return response;
    }
}
