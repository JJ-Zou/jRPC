package com.zjj.clutter.clutter;

import com.zjj.clutter.Clutter;
import com.zjj.clutter.HaStrategy;
import com.zjj.clutter.LoadBalance;
import com.zjj.clutter.balance.RoundRobinLoadBalance;
import com.zjj.clutter.ha.FailoverHaStrategy;
import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.extension.ExtensionLoader;
import com.zjj.protocol.Protocol;
import com.zjj.registry.NotifyListener;
import com.zjj.registry.RegistryFactory;
import com.zjj.rpc.Reference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ClutterNotify<T> implements NotifyListener {
    private static final RegistryFactory REGISTRY_FACTORY = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getDefaultExtension();
    private static final Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getDefaultExtension();

    private final ConcurrentMap<JRpcURL, ConcurrentMap<JRpcURL, Reference<T>>> registerReferences = new ConcurrentHashMap<>();
    private Clutter<T> clutter;
    private final Class<T> interfaceClass;
    private final Collection<JRpcURL> registryUrls;
    private final JRpcURL refUrl;

    public ClutterNotify(Class<T> interfaceClass, Collection<JRpcURL> registryUrls, JRpcURL refUrl) {
        this.interfaceClass = interfaceClass;
        this.registryUrls = registryUrls;
        this.refUrl = refUrl;
        init();
    }


    private void init() {
        prepareClutter();
        JRpcURL subscribeUrl = refUrl.deepCloneWithParameter(Collections.singletonMap(JRpcURLParamType.nodeType.getName(), JRpcURLParamType.nodeType.getName()));
        registryUrls.forEach(url -> {
            String directConnectUrl = url.getParameter(JRpcURLParamType.directConnectUrl.getName());
            if (!StringUtils.isBlank(directConnectUrl)) {
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
            param.put(JRpcURLParamType.application.getName(), url.getParameter(JRpcURLParamType.application.getName(), JRpcURLParamType.application.getValue()));
            param.put(JRpcURLParamType.module.getName(), url.getParameter(JRpcURLParamType.module.getName(), JRpcURLParamType.module.getValue()));
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
