package com.zjj.jrpc.proxy.jdk;

import com.zjj.jrpc.clutter.Clutter;
import com.zjj.jrpc.common.utils.ReflectUtils;
import com.zjj.jrpc.common.utils.RequestIdUtils;
import com.zjj.jrpc.rpc.message.DefaultRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

public class JdkInvokerHandler<T> extends AbstractInvokerHandler<T> implements InvocationHandler {

    public JdkInvokerHandler(List<Clutter<T>> clutters, Class<T> clazz) {
        super(clutters, clazz, clazz.getName());
    }

    @Override
    public Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        DefaultRequest request = DefaultRequest.builder()
                .requestId(RequestIdUtils.getRequestId())
                .interfaceName(this.interfaceName)
                .methodName(method.getName())
                .arguments(args)
                .parameterSign(ReflectUtils.getParamSigns(method))
                .build();
        return invoke(request, method.getReturnType());
    }
}
