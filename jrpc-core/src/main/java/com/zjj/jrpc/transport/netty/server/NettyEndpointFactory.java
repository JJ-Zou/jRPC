package com.zjj.jrpc.transport.netty.server;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.transport.Client;
import com.zjj.jrpc.transport.MessageHandler;
import com.zjj.jrpc.transport.Server;
import com.zjj.jrpc.transport.netty.client.NettyClient;
import com.zjj.jrpc.transport.support.AbstractEndpointFactory;

public class NettyEndpointFactory extends AbstractEndpointFactory {
    @Override
    protected Server doCreateServer(JRpcURL url, MessageHandler handler) {
        return new NettyServer(url, handler);
    }

    @Override
    protected Client doCreateClient(JRpcURL url) {
        return new NettyClient(url);
    }
}
