package com.zjj.registry.support;

import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.registry.NotifyListener;
import com.zjj.registry.ServiceListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceManager implements ServiceListener {

    private final Set<NotifyListener> notifyListeners = ConcurrentHashMap.newKeySet();
    private final Map<String, List<JRpcURL>> serviceCache = new ConcurrentHashMap<>();

    private final JRpcURL refUrl;

    private final ServiceFailbackRegistry registry;

    public ServiceManager(JRpcURL refUrl, ServiceFailbackRegistry registry) {
        log.info("ServiceManager init URL: {}", refUrl.toFullString());
        this.refUrl = refUrl;
        this.registry = registry;
    }

    public void addNotifyListener(NotifyListener listener) {
        notifyListeners.add(listener);
    }

    public void removeNotifyListener(NotifyListener listener) {
        notifyListeners.remove(listener);
    }

    @Override
    public void notifyService(JRpcURL serviceUrl, JRpcURL registryUrl, List<JRpcURL> urls) {
        String group = serviceUrl.getParameter(JRpcURLParamType.GROUP.getName(), JRpcURLParamType.GROUP.getValue());
        serviceCache.put(group, urls);
        for (NotifyListener notifyListener : notifyListeners) {
            notifyListener.notify(registry.getRegistryUrl(), urls);
        }
    }

}
