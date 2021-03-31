package com.zjj.jrpc.config.support;

import com.zjj.jrpc.clutter.Clutter;
import com.zjj.jrpc.clutter.clutter.ClutterNotify;
import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.config.ConfigHandler;
import com.zjj.jrpc.extension.ExtensionLoader;
import com.zjj.jrpc.protocol.Protocol;
import com.zjj.jrpc.proxy.ProxyFactory;
import com.zjj.jrpc.registry.Registry;
import com.zjj.jrpc.registry.RegistryFactory;
import com.zjj.jrpc.rpc.Exporter;
import com.zjj.jrpc.rpc.Provider;
import com.zjj.jrpc.rpc.support.DefaultProvider;

import java.util.Collection;
import java.util.List;

public class DefaultConfigHandler implements ConfigHandler {

    @Override
    public <T> ClutterNotify<T> getClutterNotify(Class<T> interfaceClass, Collection<JRpcURL> registryUrls, JRpcURL refUrl) {
        return new ClutterNotify<>(interfaceClass, registryUrls, refUrl);
    }

    @Override
    public <T> Exporter<T> export(Class<T> interfaceClass, T serviceRef, Collection<JRpcURL> registryUrls, JRpcURL serviceUrl) {
        Provider<T> provider = new DefaultProvider<>(interfaceClass, serviceRef, serviceUrl);
        Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getDefaultExtension();
        Exporter<T> exporter = protocol.export(provider, serviceUrl);
        RegistryFactory registryFactory = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getDefaultExtension();
        registryUrls.forEach(url -> {
            Registry registry = registryFactory.getRegistry(url);
            registry.register(serviceUrl);
        });
        return exporter;
    }

    @Override
    public <T> void unExport(Collection<Exporter<T>> exporters, Collection<JRpcURL> registryUrls) {

    }

    @Override
    public <T> T refer(Class<T> interfaceClass, List<Clutter<T>> clutters) {
        ProxyFactory factory = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getDefaultExtension();
        return factory.getProxy(interfaceClass, clutters);
    }
}
