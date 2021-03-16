package com.zjj.jrpc.proxy.cglib;

import com.zjj.jrpc.clutter.Clutter;
import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.common.utils.ReflectUtils;
import com.zjj.jrpc.common.utils.RequestIdUtils;
import com.zjj.jrpc.exception.JRpcServiceConsumerException;
import com.zjj.jrpc.rpc.Response;
import com.zjj.jrpc.rpc.message.DefaultRequest;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.List;

public class CglibMethodInterceptor<T> implements MethodInterceptor {
    protected final List<Clutter<T>> clutters;
    protected final Class<T> clazz;
    protected final String interfaceName;

    protected CglibMethodInterceptor(List<Clutter<T>> clutters, Class<T> clazz) {
        this.clutters = clutters;
        this.clazz = clazz;
        this.interfaceName = clazz.getName();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        DefaultRequest request = DefaultRequest.builder()
                .requestId(RequestIdUtils.getRequestId())
                .interfaceName(interfaceName)
                .methodName(method.getName())
                .arguments(args)
                .parameterSign(ReflectUtils.getParamSigns(method))
                .build();
        for (Clutter<T> clutter : clutters) {
            request.setAttachment(JRpcURLParamType.GROUP.getName(), clutter.getUrl().getGroup());
            request.setAttachment(JRpcURLParamType.VERSION.getName(), clutter.getUrl().getVersion());
            request.setAttachment(JRpcURLParamType.APPLICATION.getName(), clutter.getUrl().getApplication());
            request.setAttachment(JRpcURLParamType.MODULE.getName(), clutter.getUrl().getModule());
            Response response = null;
            try {
                response = clutter.call(request);
                return response.getValue();
            } catch (JRpcServiceConsumerException e) {
                throw e;
            } catch (Exception e) {
                throw e;
            }
        }
        return null;
    }
}
