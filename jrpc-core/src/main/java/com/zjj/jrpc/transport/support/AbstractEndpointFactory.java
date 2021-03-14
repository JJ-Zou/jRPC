package com.zjj.jrpc.transport.support;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.extension.ExtensionLoader;
import com.zjj.jrpc.transport.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public abstract class AbstractEndpointFactory implements EndpointFactory {

    protected final ConcurrentMap<String, Server> serverMap = new ConcurrentHashMap<>();

    private final EndpointManager endPointManager;

    protected AbstractEndpointFactory() {
        endPointManager = new HeartBeatManager();
    }

    @Override
    public Server createServer(JRpcURL url, MessageHandler handler) {
        String addressKey = url.getExportAddress();
        return serverMap.computeIfAbsent(addressKey, s -> getServer(url, handler));
    }

    private Server getServer(JRpcURL url, MessageHandler handler) {
        MessageHandler wrapHandler = ExtensionLoader
                .getExtensionLoader(HeartBeatFactory.class)
                .getDefaultExtension().wrap(handler);
        Server server = doCreateServer(url, wrapHandler);
        log.info("{} create server {} with url {}", this.getClass().getSimpleName(), server, url.getExportAddress());
        return server;
    }

    @Override
    public Client createClient(JRpcURL url) {
        Client client = doCreateClient(url);
        endPointManager.addEndPoint(client);
        return client;
    }

    @Override
    public void releaseResource(Server server, JRpcURL url) {
        destroy(server);
    }

    @Override
    public void releaseResource(Client client, JRpcURL url) {
        destroy(client);
    }

    private void destroy(Endpoint endPoint) {
        endPointManager.removeEndPoint(endPoint);
        endPoint.close();
    }

    protected abstract Server doCreateServer(JRpcURL url, MessageHandler handler);

    protected abstract Client doCreateClient(JRpcURL url);
}
