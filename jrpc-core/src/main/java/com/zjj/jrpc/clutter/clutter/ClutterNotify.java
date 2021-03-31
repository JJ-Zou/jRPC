package com.zjj.jrpc.clutter.clutter;

import com.zjj.jrpc.clutter.Clutter;
import com.zjj.jrpc.clutter.HaStrategy;
import com.zjj.jrpc.clutter.LoadBalance;
import com.zjj.jrpc.clutter.balance.RoundRobinLoadBalance;
import com.zjj.jrpc.clutter.ha.FailoverHaStrategy;
import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.extension.ExtensionLoader;
import com.zjj.jrpc.protocol.Protocol;
import com.zjj.jrpc.registry.NotifyListener;
import com.zjj.jrpc.registry.RegistryFactory;
import com.zjj.jrpc.rpc.Reference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ClutterNotify<T> implements NotifyListener {
    private static final RegistryFactory REGISTRY_FACTORY = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getDefaultExtension();
    private static final Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getDefaultExtension();

    private final ConcurrentMap<JRpcURL, ConcurrentMap<JRpcURL, Reference<T>>> registerReferences = new ConcurrentHashMap<>();
    private final Class<T> interfaceClass;
    private final Collection<JRpcURL> registryUrls;
    private final JRpcURL refUrl;
    private Clutter<T> clutter;

    public ClutterNotify(Class<T> interfaceClass, Collection<JRpcURL> registryUrls, JRpcURL refUrl) {
        this.interfaceClass = interfaceClass;
        this.registryUrls = registryUrls;
        this.refUrl = refUrl;
        init();
    }


    private void init() {
        prepareClutter();
        JRpcURL subscribeUrl = refUrl.deepCloneWithParameter(Collections.singletonMap(JRpcURLParamType.NODE_TYPE.getName(), JRpcURLParamType.NODE_TYPE.getName()));
        registryUrls.forEach(url -> {
            String directConnectUrl = url.getParameter(JRpcURLParamType.DIRECT_CONNECT_URL.getName());
            if (!StringUtils.isEmpty(directConnectUrl)) {
                // TODO: 2021/3/10 直连
            } else {
                REGISTRY_FACTORY.getRegistry(url).subscribe(subscribeUrl, this);
            }
        });
        clutter.init();
    }

    private void prepareClutter() {
        clutter = new DefaultClutter<>();
        LoadBalance<T> loadBalance = new RoundRobinLoadBalance<>();
        HaStrategy<T> haStrategy = new FailoverHaStrategy<>();
        haStrategy.setUrl(refUrl);
        clutter.setUrl(refUrl);
        clutter.setLoadBalance(loadBalance);
        clutter.setHaStrategy(haStrategy);
    }

    @Override
    public void notify(JRpcURL registryUrl, List<JRpcURL> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            log.warn("clutter config changed, urls is null. registry: [{}], url = [{}]", registryUrl.getUri(), refUrl.getIdentity());
            return;
        }
        log.info("clutter config changed. registry: [{}], url = [{}]", registryUrl.getUri(), refUrl.getIdentity());
        doRefreshReferences(registryUrl, urls);
    }

    private void doRefreshReferences(JRpcURL registryUrl, List<JRpcURL> serviceUrls) {
        serviceUrls.forEach(url -> {
            Map<String, String> param = new HashMap<>(refUrl.getParameters());
            param.put(JRpcURLParamType.APPLICATION.getName(), url.getParameter(JRpcURLParamType.APPLICATION.getName(), JRpcURLParamType.APPLICATION.getValue()));
            param.put(JRpcURLParamType.MODULE.getName(), url.getParameter(JRpcURLParamType.MODULE.getName(), JRpcURLParamType.MODULE.getValue()));
            JRpcURL clientUrl = url.deepCloneWithParameter(param);
            registerReferences
                    .computeIfAbsent(registryUrl, m -> new ConcurrentHashMap<>())
                    .put(url, protocol.refer(interfaceClass, clientUrl, url));
        });
        refreshClutters();
    }

    private void refreshClutters() {
        registerReferences.forEach((registerUrl, map) -> clutter.onRefresh(new ArrayList<>(map.values())));
    }

    public Clutter<T> getClutter() {
        return clutter;
    }

    public JRpcURL getRefUrl() {
        return refUrl;
    }
}
