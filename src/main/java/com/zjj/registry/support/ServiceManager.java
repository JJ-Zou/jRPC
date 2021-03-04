package com.zjj.registry.support;

import com.zjj.common.JRpcURL;
import com.zjj.registry.NotifyListener;
import com.zjj.registry.Registry;
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

    private final Registry registry;

    public ServiceManager(JRpcURL refUrl, Registry registry) {
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
    public void notifyService(JRpcURL refUrl, JRpcURL registryUrl, List<JRpcURL> urls) {
    }
}
