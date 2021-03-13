package com.zjj.registry;

import com.zjj.common.JRpcURL;

import java.util.List;

public interface NotifyListener {
    void notify(JRpcURL url, List<JRpcURL> urls);
}
