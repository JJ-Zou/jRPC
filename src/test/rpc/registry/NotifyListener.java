package com.zjj.rpc.registry;

import com.zjj.dubbo.common.URL;

import java.util.List;

public interface NotifyListener {
    void notify(List<URL> urls);
}
