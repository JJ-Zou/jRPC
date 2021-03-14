package com.zjj.jrpc.registry;

import com.zjj.jrpc.common.JRpcURL;

import java.util.List;

public interface DiscoveryService {
    void subscribe(JRpcURL url, NotifyListener listener);

    void unsubscribe(JRpcURL url, NotifyListener listener);

    List<JRpcURL> discover(JRpcURL url);
}
