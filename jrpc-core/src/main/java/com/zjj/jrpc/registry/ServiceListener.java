package com.zjj.jrpc.registry;

import com.zjj.jrpc.common.JRpcURL;

import java.util.List;

public interface ServiceListener {
    void notifyService(JRpcURL refUrl, JRpcURL registryUrl, List<JRpcURL> urls);
}
