package com.zjj.common.timer;

public interface TimerTask {
    void run(Timeout timeout) throws Exception;
}
