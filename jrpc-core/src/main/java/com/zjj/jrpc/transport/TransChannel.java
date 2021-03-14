package com.zjj.jrpc.transport;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.rpc.Request;
import com.zjj.jrpc.rpc.Response;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface TransChannel {
    InetSocketAddress getLocalAddress();

    InetSocketAddress getRemoteAddress();

    Response request(Request request) throws IOException;

    boolean open();

    void close();

    boolean isClosed();

    boolean isAvailable();

    JRpcURL getUrl();
}
