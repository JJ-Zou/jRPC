package com.zjj.registry;

import com.zjj.common.JRpcURL;

import java.util.List;

public interface ServiceListener {
    void notifyService(JRpcURL refUrl, JRpcURL registryUrl, List<JRpcURL> urls);
}
