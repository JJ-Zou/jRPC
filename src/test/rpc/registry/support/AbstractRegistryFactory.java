package com.zjj.rpc.registry.support;

import com.zjj.dubbo.common.URL;
import com.zjj.dubbo.registry.NotifyListener;
import com.zjj.dubbo.registry.Registry;
import com.zjj.dubbo.registry.RegistryFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class AbstractRegistryFactory implements RegistryFactory {

    protected static final Map<String, Registry> REGISTRIES = new HashMap<>();
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final AtomicBoolean destroyed = new AtomicBoolean(false);
    private static Registry DEFAULT_NOP_REGISTRY = new Registry() {
        @Override
        public URL getUrl() {
            return null;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public void destroy() {

        }

        @Override
        public void register(URL url) {

        }

        @Override
        public void unregister(URL url) {

        }

        @Override
        public void subscribe(URL url, NotifyListener listener) {

        }

        @Override
        public void unsubscribe(URL url, NotifyListener listener) {

        }

        @Override
        public List<URL> lookup(URL url) {
            return null;
        }
    };

    public static Collection<Registry> getRegistries() {
        return Collections.unmodifiableCollection(new LinkedList<>(REGISTRIES.values()));
    }

    public static Registry getRegistry(String key) {
        return REGISTRIES.get(key);
    }

    public static void destroyAll() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }
        log.debug("Close all registries {}", getRegistries());
        LOCK.lock();
        try {
            for (Registry registry : getRegistries()) {
                try {
                    registry.destroy();
                } catch (Throwable t) {
                    log.error("{}", t.getMessage(), t);
                }
            }
            REGISTRIES.clear();
        } finally {
            LOCK.unlock();
        }
    }

    public static void removeDestroyedRegistry(Registry toRm) {
        LOCK.lock();
        try {
            REGISTRIES.entrySet().removeIf(entry -> entry.getValue().equals(toRm));
        } finally {
            LOCK.unlock();
        }
    }

    @Override
    public Registry getRegistry(URL url) {
        if (destroyed.get()) {
            log.warn("All registry instances have been destroyed, failed to fetch any instance. " +
                    "Usually, this means no need to try to do unnecessary redundant resource clearance, all registries has been taken care of.");

            return DEFAULT_NOP_REGISTRY;
        }

        String key = createRegistryCacheKey(url);
        LOCK.lock();
        try {
            Registry registry = REGISTRIES.get(key);
            if (registry != null) {
                return registry;
            }
            registry = createRegistry(url);
            if (registry == null) {
                throw new IllegalStateException("Can not create registry " + url);
            }
            REGISTRIES.put(key, registry);
            return registry;
        } finally {
            LOCK.unlock();
        }
    }

    protected String createRegistryCacheKey(URL url) {
        return url.toServiceStringWithoutResolving();
    }

    protected abstract Registry createRegistry(URL url);
}
