package com.zjj.rpc.config.configcenter;

import com.zjj.rpc.common.JRpcURL;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractDynamicConfigurationFactory implements DynamicConfigurationFactory {
    private static final String DEFAULT_KEY = "default";
    private volatile Map<String, DynamicConfiguration> dynamicConfigurationMap = new ConcurrentHashMap<>();

    @Override
    public DynamicConfiguration getDynamicConfiguration(JRpcURL url) {
        String key = url == null ? DEFAULT_KEY : url.toString();
        return dynamicConfigurationMap.computeIfAbsent(key, dynamicConfiguration -> createDynamicConfiguration(url));
    }

    protected abstract DynamicConfiguration createDynamicConfiguration(JRpcURL url);
}
