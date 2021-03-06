package com.zjj.rpc.transport;

import com.zjj.rpc.Provider;
import com.zjj.rpc.Request;
import com.zjj.transport.MessageHandler;
import com.zjj.transport.TransChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
public class ProviderRouter implements MessageHandler {
    protected final ConcurrentMap<String, Provider<?>> providers = new ConcurrentHashMap<>();

    protected final AtomicInteger methodNum = new AtomicInteger(0);

    public ProviderRouter(Provider<?> provider) {
        addProvider(provider);
    }

    private void addProvider(Provider<?> provider) {
        String serviceKey = provider.getUrl().getServiceKey();
        providers.putIfAbsent(serviceKey, provider);
        List<Method> methods = Stream.of(provider.getInterface().getMethods())
                .filter(m -> m.getDeclaringClass() != Object.class)
                .collect(Collectors.toList());
        int methodCounts = methodNum.addAndGet(methods.size());
        log.info("ProviderRouter add provider {} and all public method is {}", provider, methodCounts);
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
//todo
            return null;
        }
        Method method = provider.lookupMethod(request.getMethodName(), request.getParameterSign());

        return null;
    }


}
