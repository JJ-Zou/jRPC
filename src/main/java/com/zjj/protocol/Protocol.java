package com.zjj.protocol;

import com.zjj.common.JRpcURL;
import com.zjj.rpc.Exporter;
import com.zjj.rpc.Provider;
import com.zjj.rpc.Reference;

public interface Protocol {
    <T> Exporter<T> export(Provider<T> provider, JRpcURL url);

    <T> Reference<T> refer(Class<T> clazz, JRpcURL url, JRpcURL serviceUrl);

    void destroy();
}
