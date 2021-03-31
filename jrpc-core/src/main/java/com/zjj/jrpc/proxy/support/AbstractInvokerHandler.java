package com.zjj.jrpc.proxy.support;

import com.zjj.jrpc.clutter.Clutter;
import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.common.utils.ReflectUtils;
import com.zjj.jrpc.common.utils.RequestIdUtils;
import com.zjj.jrpc.exception.JRpcServiceConsumerException;
import com.zjj.jrpc.rpc.Reference;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.rpc.Response;
import com.zjj.jrpc.rpc.context.RpcContext;
import com.zjj.jrpc.rpc.message.DefaultRequest;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractInvokerHandler<T> {
    protected final List<Clutter<T>> clutters;
    protected final Class<T> clazz;
    protected final String interfaceName;

    protected AbstractInvokerHandler(Class<T> clazz, String interfaceName, List<Clutter<T>> clutters) {
        this.clazz = clazz;
        this.interfaceName = interfaceName;
        this.clutters = clutters;
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
            builder.append("{ protocol:")
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

    Object invoke(Request request) {
        RpcContext rpcContext = RpcContext.getRpcContext();
        Map<String, String> attachments = rpcContext.getAttachments();
        attachments.forEach(request::setAttachment);
        if (!StringUtils.isEmpty(rpcContext.getRequestIdFromClient())) {
            request.setAttachment(JRpcURLParamType.REQUEST_ID_FROM_CLIENT.getName(), rpcContext.getRequestIdFromClient());
        }
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
        throw new IllegalStateException();
    }

    public Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
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
        DefaultRequest request = DefaultRequest.builder()
                .requestId(RequestIdUtils.getRequestId())
                .interfaceName(this.interfaceName)
                .methodName(method.getName())
                .arguments(args)
                .parameterSign(ReflectUtils.getParamSigns(method))
                .build();
        return invoke(request);
    }

}
