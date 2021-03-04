package com.zjj.registry.support;

import com.zjj.common.JRpcURL;
import com.zjj.registry.Registry;
import com.zjj.registry.RegistryFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractRegistryFactory implements RegistryFactory {
    private static final ConcurrentMap<String, Registry> REGISTRIES = new ConcurrentHashMap<>();

    @Override
    public Registry getRegistry(JRpcURL url) {
        String registryUri = url.getUri();
        Registry registry = REGISTRIES.get(registryUri);
        if (registry == null) {
            synchronized (AbstractRegistryFactory.class) {
                registry = REGISTRIES.get(registryUri);
                if (registry == null) {
                    registry = createRegistry(url);
                    REGISTRIES.putIfAbsent(registryUri, registry);
                }
            }
        }
        return registry;
    }

    protected abstract Registry createRegistry(JRpcURL url);
}
