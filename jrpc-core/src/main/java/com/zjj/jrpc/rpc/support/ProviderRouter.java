package com.zjj.jrpc.rpc.support;

import com.zjj.jrpc.common.utils.ReflectUtils;
import com.zjj.jrpc.exception.JRpcErrorMessage;
import com.zjj.jrpc.exception.JRpcServiceProviderException;
import com.zjj.jrpc.extension.ExtensionLoader;
import com.zjj.jrpc.rpc.Provider;
import com.zjj.jrpc.rpc.ProviderProtectedStrategy;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.rpc.Response;
import com.zjj.jrpc.rpc.message.DefaultRequest;
import com.zjj.jrpc.rpc.message.DefaultResponse;
import com.zjj.jrpc.transport.MessageHandler;
import com.zjj.jrpc.transport.TransChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
public class ProviderRouter implements MessageHandler {

    protected static final ConcurrentMap<String, Provider<?>> providers = new ConcurrentHashMap<>();

    protected final AtomicInteger methodNum = new AtomicInteger(0);

    private final ProviderProtectedStrategy strategy;

    public ProviderRouter() {
        strategy = ExtensionLoader.getExtensionLoader(ProviderProtectedStrategy.class).getDefaultExtension();
    }

    public void addProvider(Provider<?> provider) {
        if (provider == null) {
            throw new JRpcServiceProviderException("ProviderRouter receive null provider", JRpcErrorMessage.FRAMEWORK_SERVER_ERROR);
        }
        String serviceKey = provider.getUrl().getServiceKey();
        providers.putIfAbsent(serviceKey, provider);
        List<Method> methods = Stream.of(provider.getInterface().getMethods())
                .filter(m -> m.getDeclaringClass() != Object.class)
                .collect(Collectors.toList());
        int methodCounts = methodNum.addAndGet(methods.size());
        log.info("ProviderRouter add provider {} and all public method is {}", provider, methodCounts);
    }

    public void removeProvider(Provider<?> provider) {
        String serviceKey = provider.getUrl().getServiceKey();
        providers.remove(serviceKey);
        List<Method> methods = Stream.of(provider.getInterface().getMethods())
                .filter(m -> m.getDeclaringClass() != Object.class)
                .collect(Collectors.toList());
        int methodCounts = methodNum.addAndGet(-methods.size());
        log.info("ProviderRouter remove provider {} and all public method is {}", provider, methodCounts);
    }

    @Override
    public Object handler(TransChannel transChannel, Object message) {
        if (transChannel == null || !(message instanceof Request)) {
            throw new IllegalArgumentException("Arguments maybe illegal.");
        }
        Request request = (Request) message;
        String serviceKey = request.getServiceKey();
        Provider<?> provider = providers.get(serviceKey);
        if (provider == null) {
            log.error("{} handler request [{}] error, provider not exist service key = {}.", this.getClass().getSimpleName(), request, serviceKey);
            IllegalStateException exception = new IllegalStateException(this.getClass().getSimpleName() + " handler request " + request + " error, provider not exist service key = " + serviceKey + ".");
            return DefaultResponse.builder().exception(exception).requestId(request.getRequestId()).protocolVersion(request.getProtocolVersion()).build();
        }
        Method method = provider.lookupMethod(request.getMethodName(), request.getParameterSign());
        appendMethodSign(request, method);
        Response response = call(request, provider);
        response.setProtocolVersion(request.getProtocolVersion());
        response.setSerializeNumber(request.getSerializeNumber());
        return response;
    }

    private void appendMethodSign(Request request, Method method) {
        if (method == null || !(request instanceof DefaultRequest) || !StringUtils.isEmpty(request.getParameterSign())) {
            return;
        }
        ((DefaultRequest) request).setParameterSign(ReflectUtils.getMethodSign(method));
        ((DefaultRequest) request).setMethodName(method.getName());
    }

    private Response call(Request request, Provider<?> provider) {
        try {
            return strategy.call(request, provider);
        } catch (Exception e) {
            return DefaultResponse.builder()
                    .exception(new IllegalStateException("provider call process error", e))
                    .requestId(request.getRequestId())
                    .protocolVersion(request.getProtocolVersion())
                    .build();
        }
    }
}
