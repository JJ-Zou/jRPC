package com.zjj.jrpc.transport;

import com.zjj.jrpc.common.JRpcURL;

public interface EndpointFactory {
    Server createServer(JRpcURL url, MessageHandler handler);

    Client createClient(JRpcURL url);

    void releaseResource(Server server, JRpcURL url);

    void releaseResource(Client client, JRpcURL url);
}
