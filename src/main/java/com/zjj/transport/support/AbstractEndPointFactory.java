package com.zjj.transport.support;

import com.zjj.common.JRpcURL;
import com.zjj.extension.ExtensionLoader;
import com.zjj.transport.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractEndPointFactory implements EndpointFactory {

    private final EndPointManager endPointManager;

    protected AbstractEndPointFactory() {
        endPointManager = new HeartBeatManager();
    }

    @Override
    public Server createServer(JRpcURL url, MessageHandler handler) {
        MessageHandler wrapHandler = ExtensionLoader
                .getExtensionLoader(HeartBeatFactory.class)
                .getDefaultExtension().wrap(handler);
        Server server = doCreateServer(url, wrapHandler);
        log.info("{} create server {} with url {}", this.getClass().getSimpleName(), server, url);
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

    private void destroy(EndPoint endPoint) {
        endPointManager.removeEndPoint(endPoint);
        endPoint.close();
    }

    protected abstract Server doCreateServer(JRpcURL url, MessageHandler handler);

    protected abstract Client doCreateClient(JRpcURL url);
}
