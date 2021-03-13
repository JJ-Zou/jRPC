package com.zjj.registry;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.registry.zookeeper.ZookeeperRegistry;
import com.zjj.registry.zookeeper.ZookeeperRegistryFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TestRegistry {
    @Test
    public void registry() throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("address", "39.105.65.104:2181");
        parameters.put(JRpcURLParamType.REGISTRY_RETRY_PERIOD.getName(), "1000");
        JRpcURL jRpcURL = new JRpcURL("META_INF/jrpc", "127.0.0.1", 20855, "com.zjj.registry.zookeeper", parameters);
        ZookeeperRegistryFactory registryFactory = new ZookeeperRegistryFactory();
        Method createRegistry = registryFactory.getClass().getDeclaredMethod("createRegistry", JRpcURL.class);
        createRegistry.setAccessible(true);
        ZookeeperRegistry registry = (ZookeeperRegistry) createRegistry.invoke(registryFactory, jRpcURL);
        registry.register(jRpcURL);
        Method create = registry.getClass().getDeclaredMethod("create", String.class, boolean.class);
        create.setAccessible(true);
        create.invoke(registry, "/META_INF/jrpc/default_rpc/com.zjj.registry.zookeeper/service/132.22.22.1:9999", false);
        registry.subscribe(jRpcURL, (url, urls) -> log.info("Test: {}, {}", url, urls));
        System.out.println(registry.discover(jRpcURL));
        registry.unsubscribe(jRpcURL, (url, urls) -> log.info("Test: {}, {}", url, urls));
    }
}
