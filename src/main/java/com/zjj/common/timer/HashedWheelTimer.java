package com.zjj.common.timer;

import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class HashedWheelTimer implements Timer{
    public HashedWheelTimer(ThreadFactory threadFactory) {

    }

    @Override
    public Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
        return null;
    }

    @Override
    public Set<Timeout> stop() {
        return null;
    }

    @Override
    public boolean isStop() {
        return false;
    }
}
