package com.zjj.jrpc.config.support;

import com.zjj.jrpc.config.AbstractConfig;
import com.zjj.jrpc.config.ProtocolConfig;
import com.zjj.jrpc.config.RegistryConfig;
import com.zjj.jrpc.config.spring.ServiceBean;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigBeanManager {
    protected static final ConcurrentMap<String, Map<String, AbstractConfig>> ABSTRACT_CONFIG_MAP = new ConcurrentHashMap();

    private ConfigBeanManager() {
    }

    public static void addAbstractConfig(String beanName, AbstractConfig config) {
        String tagName = config.getTagName();
        ABSTRACT_CONFIG_MAP.computeIfAbsent(tagName, s -> new ConcurrentHashMap<>())
                .put(beanName, config);
    }

    public static Map<String, Map<String, AbstractConfig>> getAbstractConfigMap() {
        return Collections.unmodifiableMap(ABSTRACT_CONFIG_MAP);
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractConfig> Map<String, T> getConfigs(String configType) {
        return (Map<String, T>) Collections.unmodifiableMap(ABSTRACT_CONFIG_MAP.get(configType));
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> getConfigsMap(Class<T> clazz) {
        return (Map<String, T>) Collections.unmodifiableMap(ABSTRACT_CONFIG_MAP.get(AbstractConfig.getTagName(clazz)));
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> getConfigs(Class<T> clazz) {
        return (Collection<T>) Collections.unmodifiableCollection(ABSTRACT_CONFIG_MAP.computeIfAbsent(AbstractConfig.getTagName(clazz), m -> new HashMap<>()).values());
    }

    @SuppressWarnings("unchecked")
    public static Collection<ServiceBean> getServiceBeans() {
        return getConfigs(ServiceBean.class);
    }

    public static Collection<RegistryConfig> getRegistryConfigs() {
        return getConfigs(RegistryConfig.class);
    }

    public static Collection<ProtocolConfig> getProtocolConfigs() {
        return getConfigs(ProtocolConfig.class);
    }
}
