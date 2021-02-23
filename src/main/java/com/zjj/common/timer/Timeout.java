package com.zjj.common.timer;

public interface Timeout {

    Timer timer();

    TimerTask task();

    boolean isExpired();

    boolean isCanceled();
    /**
     * 尝试取消TimerTask
     * @return 取消成功返回true
     */
    boolean cancel();
}
