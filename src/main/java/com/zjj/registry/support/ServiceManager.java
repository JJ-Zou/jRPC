package com.zjj.registry.support;

import com.zjj.common.JRpcURL;
import com.zjj.registry.NotifyListener;
import com.zjj.registry.ServiceListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceManager implements ServiceListener {

    private final Set<NotifyListener> notifyListeners = ConcurrentHashMap.newKeySet();
    private JRpcURL refUrl;

    private ServiceFailbackRegistry registry;

    @Override
    public void notifyService(JRpcURL refUrl, JRpcURL registryUrl, List<JRpcURL> urls) {

    }
}
