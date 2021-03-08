package com.zjj.transport.netty.server;

import com.zjj.common.JRpcURL;
import com.zjj.transport.Client;
import com.zjj.transport.MessageHandler;
import com.zjj.transport.Server;
import com.zjj.transport.netty.client.NettyClient;
import com.zjj.transport.support.AbstractEndpointFactory;

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
