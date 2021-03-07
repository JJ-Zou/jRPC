package com.zjj.protocol.support;

import com.zjj.common.JRpcURL;
import com.zjj.rpc.Exporter;
import com.zjj.rpc.Provider;
import com.zjj.rpc.Reference;
import com.zjj.rpc.support.DefaultExporter;
import com.zjj.rpc.support.DefaultReference;

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
