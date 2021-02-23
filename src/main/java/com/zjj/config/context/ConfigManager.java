package com.zjj.config.context;

import com.zjj.common.context.FrameworkExt;
import com.zjj.common.context.LifecycleAdapter;
import com.zjj.config.AbstractConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class ConfigManager extends LifecycleAdapter implements FrameworkExt {
    public static final String NAME = "config";

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    final Map<String, Map<String, AbstractConfig>> configsCache = new HashMap<>();

    public ConfigManager() {
    }


}
