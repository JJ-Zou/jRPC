package com.zjj.rpc.registry.retry;

import com.zjj.dubbo.common.URL;
import com.zjj.dubbo.common.timer.Timeout;
import com.zjj.dubbo.registry.Constants;
import com.zjj.dubbo.registry.NotifyListener;
import com.zjj.dubbo.registry.support.FailbackRegistry;

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
