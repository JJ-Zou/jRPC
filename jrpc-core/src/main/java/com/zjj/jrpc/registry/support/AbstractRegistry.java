package com.zjj.jrpc.registry.support;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.registry.NotifyListener;
import com.zjj.jrpc.registry.Registry;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractRegistry implements Registry {

    protected final String registryClassName = this.getClass().getSimpleName();
    private final ConcurrentMap<JRpcURL, Map<String, List<JRpcURL>>> subscribedResponses = new ConcurrentHashMap<>();
    private final JRpcURL registryUrl;
    private final Set<JRpcURL> registeredServiceUrls = ConcurrentHashMap.newKeySet();


    protected AbstractRegistry(JRpcURL url) {
        this.registryUrl = url;
    }

    @Override
    public JRpcURL getRegistryUrl() {
        return registryUrl;
    }


    @Override
    public void register(JRpcURL url) {
        if (url == null) {
            throw new IllegalArgumentException("url == null.");
        }
        log.info("[{}] url: [{}] will register to registry {}", registryClassName, url, registryUrl.getIdentity());
        doRegister(url);
        registeredServiceUrls.add(url);
        available(url);
    }

    @Override
    public void unregister(JRpcURL url) {
        if (url == null) {
            throw new IllegalArgumentException("url == null.");
        }
        log.info("[{}] url: [{}] will unregister from registry {}", registryClassName, url, registryUrl.getIdentity());
        doUnregister(url);
        registeredServiceUrls.remove(url);
    }


    @Override
    public void available(JRpcURL url) {
        log.info("[{}] url: [{}] will set available to registry {}", registryClassName, url, registryUrl.getIdentity());
        doAvailable(url);
    }

    @Override
    public void unavailable(JRpcURL url) {
        log.info("[{}] url: [{}] will set unavailable to registry {}", registryClassName, url, registryUrl.getIdentity());
        doUnavailable(url);
    }

    @Override
    public Collection<JRpcURL> getRegisteredServices() {
        return Collections.unmodifiableCollection(registeredServiceUrls);
    }

    @Override
    public void subscribe(JRpcURL url, NotifyListener listener) {
        if (url == null || listener == null) {
            log.warn("Illegal argument, url = {}, listener = {}", url, listener);
            return;
        }
        log.info("[{}] listener: [{}] will subscribe to registry {} with url: {}", registryClassName, listener, registryUrl.getIdentity(), url);
        doSubscribe(url, listener);
    }

    @Override
    public void unsubscribe(JRpcURL url, NotifyListener listener) {
        if (url == null || listener == null) {
            log.warn("Illegal argument, url = {}, listener = {}", url, listener);
            return;
        }
        log.info("[{}] listener: [{}] will unsubscribe from registry {} with url: {}", registryClassName, listener, registryUrl.getIdentity(), url);
        doUnsubscribe(url, listener);
    }

    @Override
    public List<JRpcURL> discover(JRpcURL url) {
        if (url == null) {
            log.warn("url = null.");
            return Collections.emptyList();
        }
        Map<String, List<JRpcURL>> map = subscribedResponses.get(url);
        if (map == null || map.isEmpty()) {
            return doDiscover(url);
        }
        List<JRpcURL> result = new ArrayList<>();
        for (List<JRpcURL> value : map.values()) {
            result.addAll(value);
        }
        return result;
    }

    protected void notify(JRpcURL refUrl, NotifyListener listener, List<JRpcURL> urls) {
        if (listener == null || urls == null) {
            return;
        }
        Map<String, List<JRpcURL>> nodeTypeUrls = urls.stream().collect(Collectors.groupingBy(url ->
                url.getParameter(JRpcURLParamType.NODE_TYPE.getName(), JRpcURLParamType.NODE_TYPE.getValue())));
        subscribedResponses.computeIfAbsent(refUrl, m -> new ConcurrentHashMap<>()).putAll(nodeTypeUrls);
        nodeTypeUrls.values().forEach(us -> listener.notify(getRegistryUrl(), us));
    }

    protected List<JRpcURL> getCachedUrls(JRpcURL url) {
        Map<String, List<JRpcURL>> listMap = subscribedResponses.get(url);
        if (listMap == null || listMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<JRpcURL> result = new ArrayList<>();
        for (List<JRpcURL> value : listMap.values()) {
            result.addAll(value);
        }
        return result;
    }

    protected abstract void doRegister(JRpcURL url);

    protected abstract void doUnregister(JRpcURL url);

    protected abstract void doSubscribe(JRpcURL url, NotifyListener listener);

    protected abstract void doUnsubscribe(JRpcURL url, NotifyListener listener);

    protected abstract void doAvailable(JRpcURL url);

    protected abstract void doUnavailable(JRpcURL url);

    protected abstract List<JRpcURL> doDiscover(JRpcURL url);
}
