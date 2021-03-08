package com.zjj.rpc.support;

import com.zjj.common.JRpcURL;
import com.zjj.extension.ExtensionLoader;
import com.zjj.protocol.support.AbstractProtocol;
import com.zjj.rpc.Provider;
import com.zjj.transport.EndpointFactory;
import com.zjj.transport.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class DefaultExporter<T> extends AbstractExporter<T> {

    private static final ConcurrentMap<String, ProviderRouter> PROVIDER_ROUTERS = new ConcurrentHashMap<>();

    protected final EndpointFactory endpointFactory;
    protected final Server server;

    public DefaultExporter(JRpcURL url, Provider<T> provider) {
        super(url, provider);
        ProviderRouter providerRouter = obtainProviderRouter(url);
        endpointFactory = ExtensionLoader.getExtensionLoader(EndpointFactory.class).getDefaultExtension();
        server = endpointFactory.createServer(url, providerRouter);
    }

    @Override
    protected boolean doInit() {
        return server.open();
    }

    @Override
    public void unExport() {
        String protocolKey = url.getProtocolKey();
        String address = url.getAddress();
        AbstractProtocol.destroy(protocolKey);
        if (PROVIDER_ROUTERS.containsKey(address)) {
            PROVIDER_ROUTERS.get(address).removeProvider(provider);
        }
        log.info("DefaultExporter unExport url {}", url);
    }


    @Override
    public void destroy() {
        endpointFactory.releaseResource(server, url);
    }

    @Override
    public boolean isAvailable() {
        return server.isAvailable();
    }

    protected ProviderRouter obtainProviderRouter(JRpcURL url) {
        String address = url.getAddress();
        return PROVIDER_ROUTERS.computeIfAbsent(address, p -> new ProviderRouter(provider));
    }
}
