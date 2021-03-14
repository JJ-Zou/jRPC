package com.zjj.jrpc.registry;

import com.zjj.jrpc.common.JRpcURL;

import java.util.List;

public interface NotifyListener {
    void notify(JRpcURL url, List<JRpcURL> urls);
}
