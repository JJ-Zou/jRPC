package com.zjj.rpc.transport;

import com.zjj.common.JRpcURL;
import com.zjj.extension.ExtensionLoader;
import com.zjj.rpc.Exporter;
import com.zjj.rpc.Provider;
import com.zjj.transport.EndpointFactory;
import com.zjj.transport.Server;

import java.util.concurrent.ConcurrentMap;

public class DefaultExporter<T> extends AbstractExporter<T> {

    protected final ConcurrentMap<String, Exporter<?>> exporters;

    protected final EndpointFactory endpointFactory;
    protected final Server server;

    protected DefaultExporter(JRpcURL url, Provider<T> provider, ConcurrentMap<String, Exporter<?>> exporters) {
        super(url, provider);
        this.exporters = exporters;
        endpointFactory = ExtensionLoader.getExtensionLoader(EndpointFactory.class).getDefaultExtension();
        server = endpointFactory.createServer(url, null);
    }

    @Override
    protected boolean doInit() {
        return server.open();
    }

    @Override
    public void unExport() {
        String address = url.getAddress();

    }


    @Override
    public void destroy() {
        endpointFactory.releaseResource(server, url);
    }

    @Override
    public boolean isAvailable() {
        return server.isAvailable();
    }
}
