package com.zjj.rpc.config.context;

import com.zjj.rpc.config.AbstractConfig;
import com.zjj.rpc.config.ServiceConfigBase;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConfigManager {
    static final Map<String, Map<String, AbstractConfig>> configsCache = new ConcurrentHashMap<>();
    private static final String BEAN_NAME = "ConfigManager";

    private ConfigManager() {
    }

    public static void addConfig(AbstractConfig config) {
        configsCache.computeIfAbsent(AbstractConfig.getTagName(config.getClass()), m -> new ConcurrentHashMap<>())
                .putIfAbsent(config.getBeanName(), config);
    }

    public static AbstractConfig getConfig(Class<?> clz, String beanName) {
        return configsCache.getOrDefault(AbstractConfig.getTagName(clz), new ConcurrentHashMap<>()).get(beanName);
    }

    public static Collection<ServiceConfigBase> getServices() {
        return getConfigs(AbstractConfig.getTagName(ServiceConfigBase.class));
    }

    public static <C extends AbstractConfig> Collection<C> getConfigs(String type) {
        return (Collection<C>) configsCache.getOrDefault(type, new ConcurrentHashMap<>()).values();
    }
}
