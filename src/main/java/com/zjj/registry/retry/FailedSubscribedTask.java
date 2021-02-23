package com.zjj.registry.retry;

import com.zjj.common.URL;
import com.zjj.registry.Constants;
import com.zjj.registry.NotifyListener;
import com.zjj.registry.support.FailbackRegistry;
import com.zjj.common.timer.Timeout;

public class FailedSubscribedTask extends AbstractRetryTask {
    private final NotifyListener listener;

    public FailedSubscribedTask(URL url, FailbackRegistry registry, NotifyListener listener) {
        super(url, registry, Constants.RETRY_SUBSCRIBE_NAME);
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        this.listener = listener;
    }

    @Override
    protected void doRetry(URL url, FailbackRegistry registry, Timeout timeout) {
        registry.doSubscribe(url, listener);
        registry.removeFailedSubscribedTask(url, listener);
    }
}
