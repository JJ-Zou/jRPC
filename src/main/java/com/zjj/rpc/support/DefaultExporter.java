package com.zjj.rpc.support;

import com.zjj.common.JRpcURL;
import com.zjj.extension.ExtensionLoader;
import com.zjj.protocol.Protocol;
import com.zjj.rpc.Provider;
import com.zjj.transport.EndpointFactory;
import com.zjj.transport.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class DefaultExporter<T> extends AbstractExporter<T> {
    private static final Protocol PROTOCOL = ExtensionLoader.getExtensionLoader(Protocol.class).getDefaultExtension();
    private static final ConcurrentMap<String, ProviderRouter> PROVIDER_ROUTERS = new ConcurrentHashMap<>();

    protected static final EndpointFactory ENDPOINT_FACTORY = ExtensionLoader.getExtensionLoader(EndpointFactory.class).getDefaultExtension();
    protected final Server server;

    public DefaultExporter(JRpcURL url, Provider<T> provider) {
        super(url, provider);
        ProviderRouter providerRouter = obtainProviderRouter(url);
        server = ENDPOINT_FACTORY.createServer(url, providerRouter);
    }

    @Override
    protected boolean doInit() {
        return server.open();
    }

    @Override
    public void unExport() {
        String protocolKey = url.getProtocolKey();
        String address = url.getAddress();
        PROTOCOL.destroy(protocolKey);
        if (PROVIDER_ROUTERS.containsKey(address)) {
            PROVIDER_ROUTERS.get(address).removeProvider(provider);
        }
        log.info("DefaultExporter unExport url {}", url);
    }

    @Override
    public void destroy() {
        ENDPOINT_FACTORY.releaseResource(server, url);
    }

    @Override
    public boolean isAvailable() {
        return server.isAvailable();
    }

    protected ProviderRouter obtainProviderRouter(JRpcURL url) {
        String key = url.getAddress();
        ProviderRouter providerRouter = PROVIDER_ROUTERS.computeIfAbsent(key, p -> new ProviderRouter());
        providerRouter.addProvider(provider);
        return providerRouter;
    }
}
