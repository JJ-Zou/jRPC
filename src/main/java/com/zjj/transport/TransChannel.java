package com.zjj.transport;

import com.zjj.common.JRpcURL;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;

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
