package com.zjj.proxy.support;

import com.zjj.clutter.Clutter;
import com.zjj.common.JRpcURLParamType;
import com.zjj.rpc.Reference;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import com.zjj.rpc.context.RpcContext;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractInvokerHandler<T> implements InvocationHandler {
    protected final List<Clutter<T>> clutters;
    protected final Class<T> clazz;
    protected final String interfaceName;

    protected AbstractInvokerHandler(List<Clutter<T>> clutters, Class<T> clazz, String interfaceName) {
        this.clutters = clutters;
        this.clazz = clazz;
        this.interfaceName = interfaceName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() != this.clazz) {
            switch (method.getName()) {
                case "toString":
                    return proxyToString();
                case "hashCode":
                    return proxyHashcode(args[0]);
                case "equals":
                    return proxyEquals(args[0]);
                default:
                    throw new IllegalStateException("Unexpected value: " + method.getName());
            }
        }
        return doInvoke(proxy, method, args);
    }

    public boolean proxyEquals(Object o) {
        return false;
    }

    public int proxyHashcode(Object o) {
        return Objects.hashCode(o);
    }

    public String proxyToString() {
        StringBuilder builder = new StringBuilder();
        for (Clutter<T> clutter : clutters) {
            builder.append("{protocol:")
                    .append(clutter.getUrl().getProtocol());
            List<Reference<T>> references = clutter.getReferences();
            if (references != null) {
                for (Reference<T> reference : references) {
                    builder.append("[")
                            .append(reference.getUrl().toSimpleString())
                            .append(", available:")
                            .append(reference.isAvailable())
                            .append("] ");
                }
            }
            builder.append(" }");
        }
        return builder.toString();
    }

    Object invoke(Request request, Class<?> returnType) {
        RpcContext rpcContext = RpcContext.getRpcContext();
        Map<String, String> attachments = rpcContext.getAttachments();
        attachments.forEach(request::setAttachment);
        if (StringUtils.isNotBlank(rpcContext.getRequestIdFromClient())) {
            request.setAttachment(JRpcURLParamType.requestIdFromClient.getName(), rpcContext.getRequestIdFromClient());
        }
        for (Clutter<T> clutter : clutters) {
            request.setAttachment(JRpcURLParamType.group.getName(), clutter.getUrl().getGroup());
            request.setAttachment(JRpcURLParamType.version.getName(), clutter.getUrl().getVersion());
            request.setAttachment(JRpcURLParamType.application.getName(), clutter.getUrl().getApplication());
            request.setAttachment(JRpcURLParamType.module.getName(), clutter.getUrl().getModule());

            Response response;
            try {
                response = clutter.call(request);
                return response.getValue();
            } catch (Exception e) {

            }
        }
        throw new IllegalStateException();
    }

    public abstract Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable;
}
