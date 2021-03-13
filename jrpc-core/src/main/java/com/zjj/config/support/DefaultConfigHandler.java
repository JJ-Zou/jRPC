package com.zjj.config.support;

import com.zjj.clutter.Clutter;
import com.zjj.clutter.clutter.ClutterNotify;
import com.zjj.common.JRpcURL;
import com.zjj.config.ConfigHandler;
import com.zjj.extension.ExtensionLoader;
import com.zjj.protocol.Protocol;
import com.zjj.proxy.ProxyFactory;
import com.zjj.registry.Registry;
import com.zjj.registry.RegistryFactory;
import com.zjj.rpc.Exporter;
import com.zjj.rpc.Provider;
import com.zjj.rpc.support.DefaultProvider;

import java.util.Collection;
import java.util.List;

public class DefaultConfigHandler implements ConfigHandler {

    private static final ProxyFactory PROXY_FACTORY = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getDefaultExtension();
    private static final Protocol PROTOCOL = ExtensionLoader.getExtensionLoader(Protocol.class).getDefaultExtension();

    @Override
    public <T> ClutterNotify<T> getClutterNotify(Class<T> interfaceClass, Collection<JRpcURL> registryUrls, JRpcURL refUrl) {
        return new ClutterNotify<>(interfaceClass, registryUrls, refUrl);
    }

    @Override
    public <T> Exporter<T> export(Class<T> interfaceClass, T ref, Collection<JRpcURL> registryUrls, JRpcURL refUrl) {
        Provider<T> provider = new DefaultProvider<>(interfaceClass, ref, refUrl);
        Exporter<T> exporter = PROTOCOL.export(provider, refUrl);
        RegistryFactory registryFactory = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getDefaultExtension();
        registryUrls.forEach(url -> {
            Registry registry = registryFactory.getRegistry(url);
            registry.register(refUrl);
        });
        return exporter;
    }

    @Override
    public <T> void unExport(Collection<Exporter<T>> exporters, Collection<JRpcURL> registryUrls) {

    }

    @Override
    public <T> T refer(Class<T> interfaceClass, List<Clutter<T>> clutters) {
        return PROXY_FACTORY.getProxy(interfaceClass, clutters);
    }
}
