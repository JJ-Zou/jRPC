package com.zjj.registry.retry;

import com.zjj.common.URL;
import com.zjj.registry.Constants;
import com.zjj.registry.NotifyListener;
import com.zjj.registry.support.FailbackRegistry;
import com.zjj.common.timer.Timeout;

public class FailedUnsubscribedTask extends AbstractRetryTask {
    private final NotifyListener listener;

    public FailedUnsubscribedTask(URL url, FailbackRegistry registry, NotifyListener listener) {
        super(url, registry, Constants.RETRY_UNSUBSCRIBE_NAME);
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        this.listener = listener;
    }

    @Override
    protected void doRetry(URL url, FailbackRegistry registry, Timeout timeout) {
        registry.doUnsubscribe(url, listener);
        registry.removeFailedUnsubscribedTask(url, listener);
    }
}
