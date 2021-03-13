package com.zjj.registry;

import com.zjj.common.JRpcURL;

import java.util.List;

public interface DiscoveryService {
    void subscribe(JRpcURL url, NotifyListener listener);

    void unsubscribe(JRpcURL url, NotifyListener listener);

    List<JRpcURL> discover(JRpcURL url);
}
