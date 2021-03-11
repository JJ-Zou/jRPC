package com.zjj.protocol.support;

import com.zjj.common.JRpcURL;
import com.zjj.protocol.Protocol;
import com.zjj.rpc.Exporter;
import com.zjj.rpc.Provider;
import com.zjj.rpc.Reference;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public abstract class AbstractProtocol implements Protocol {
    protected final ConcurrentMap<String, Exporter<?>> exporters = new ConcurrentHashMap<>();


    @Override
    public <T> Exporter<T> export(Provider<T> provider, JRpcURL url) {
        String protocolKey = url.getProtocolKey();
        Exporter<T> exporter = (Exporter<T>) exporters.computeIfAbsent(protocolKey, e -> doExport(provider, url));
        exporter.init();
        log.info("{} export service: {} success.", this.getClass().getSimpleName(), url);
        return exporter;
    }

    @Override
    public <T> Reference<T> refer(Class<T> clazz, JRpcURL url, JRpcURL serviceUrl) {
        long start = System.currentTimeMillis();
        Reference<T> reference = doRefer(clazz, url, serviceUrl);
        reference.init();
        log.info("{} refer service: {} success, cost {} ms.", this.getClass().getSimpleName(), url, System.currentTimeMillis() - start);
        return reference;
    }

    protected abstract <T> Exporter<T> doExport(Provider<T> provider, JRpcURL url);

    protected abstract <T> Reference<T> doRefer(Class<T> clazz, JRpcURL url, JRpcURL serviceUrl);

    public void destroy(String protocolKey) {
        if (!exporters.containsKey(protocolKey)) {
            return;
        }
        exporters.remove(protocolKey).destroy();
    }

    @Override
    public void destroy() {
        exporters.forEach((k, v) -> v.destroy());
        exporters.clear();
    }
}
