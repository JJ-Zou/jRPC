package com.zjj.registry;

import com.zjj.common.URL;

import java.util.List;

public interface NotifyListener {
    void notify(List<URL> urls);
}
