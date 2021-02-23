package com.zjj.common.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NameThreadFactory implements ThreadFactory {
    protected static final AtomicInteger POOL_SEQ = new AtomicInteger(1);
    protected final AtomicInteger mThreadNum = new AtomicInteger(1);
    protected final String mPrefix;
    protected final boolean mDaemon;
    protected final ThreadGroup mGroup;


    public NameThreadFactory() {
        this("pool-" + POOL_SEQ.getAndIncrement(), false);
    }

    public NameThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NameThreadFactory(String prefix, boolean daemon) {
        this.mPrefix = prefix + "-thread-";
        this.mDaemon = daemon;
        SecurityManager s = System.getSecurityManager();
        this.mGroup = s == null ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = mPrefix + mThreadNum.getAndIncrement();
        Thread thread = new Thread(mGroup, r, name, 0);
        thread.setDaemon(mDaemon);
        return thread;
    }

    public ThreadGroup getThreadGroup() {
        return mGroup;
    }
}
