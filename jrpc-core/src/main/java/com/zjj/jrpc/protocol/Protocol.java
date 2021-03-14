package com.zjj.jrpc.protocol;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.rpc.Exporter;
import com.zjj.jrpc.rpc.Provider;
import com.zjj.jrpc.rpc.Reference;

public interface Protocol {
    <T> Exporter<T> export(Provider<T> provider, JRpcURL url);

    <T> Reference<T> refer(Class<T> clazz, JRpcURL url, JRpcURL serviceUrl);

    void destroy(String protocolKey);

    void destroy();
}
