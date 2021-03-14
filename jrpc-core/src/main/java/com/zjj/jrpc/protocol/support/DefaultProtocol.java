package com.zjj.jrpc.protocol.support;

import com.zjj.jrpc.common.JRpcURL;
import com.zjj.jrpc.rpc.Exporter;
import com.zjj.jrpc.rpc.Provider;
import com.zjj.jrpc.rpc.Reference;
import com.zjj.jrpc.rpc.support.DefaultExporter;
import com.zjj.jrpc.rpc.support.DefaultReference;

public class DefaultProtocol extends AbstractProtocol {
    @Override
    protected <T> Exporter<T> doExport(Provider<T> provider, JRpcURL url) {
        return new DefaultExporter<>(url, provider);
    }

    @Override
    protected <T> Reference<T> doRefer(Class<T> clazz, JRpcURL url, JRpcURL serviceUrl) {
        return new DefaultReference<>(clazz, url, serviceUrl);
    }
}
