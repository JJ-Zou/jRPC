package com.zjj.registry.retry;

import com.zjj.common.URL;
import com.zjj.common.timer.Timeout;
import com.zjj.common.timer.Timer;
import com.zjj.common.timer.TimerTask;
import com.zjj.common.utils.StringUtils;
import com.zjj.registry.Constants;
import com.zjj.registry.support.FailbackRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractRetryTask implements TimerTask {
    protected final URL url;

    protected final FailbackRegistry registry;

    // 重试间隔
    final long retryPeriod;

    // 最大重试次数
    private final int retryTimes;

    private final String taskName;

    // 重试次数
    private int times = 1;

    private volatile boolean cancel;

    AbstractRetryTask(URL url, FailbackRegistry registry, String taskName) {
        if (url == null || StringUtils.isBlank(taskName)) {
            throw new IllegalArgumentException();
        }
        this.url = url;
        this.registry = registry;
        this.taskName = taskName;
        cancel = false;
        this.retryPeriod = url.getParameter(Constants.REGISTRY_RETRY_PERIOD_KEY, Constants.DEFAULT_REGISTRY_RETRY_PERIOD);
        this.retryTimes = url.getParameter(Constants.REGISTRY_RETRY_TIMES_KEY, Constants.DEFAULT_REGISTRY_RETRY_TIMES);
    }

    public void cancel() {
        cancel = true;
    }

    public boolean isCancel() {
        return cancel;
    }

    protected void reput(Timeout timeout, long tick) {
        if (timeout == null) {
            throw new IllegalArgumentException();
        }
        Timer timer = timeout.timer();
        if (timer.isStop() || timeout.isCanceled() || isCancel()) {
            return;
        }
        times++;
        timer.newTimeout(timeout.task(), tick, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        if (timeout.isCanceled() || timeout.timer().isStop() || isCancel()) {
            return;
        }
        if (times > retryTimes) {
            log.warn("Final failed to execute task {}, url: {}, retry {} times.",
                    taskName, url, retryTimes);
            return;
        }
        log.debug("{} : {}", taskName, url);
        try {
            doRetry(url, registry, timeout);
        } catch (Throwable t) {
            log.warn("Failed to execute task {}, url: {}, waiting for again, cause: {}",
                    taskName, url, t.getMessage(), t);
            reput(timeout, retryPeriod);
        }
    }

    protected abstract void doRetry(URL url, FailbackRegistry registry, Timeout timeout);
}
