package com.zjj.rpc.registry.retry;

import com.zjj.dubbo.common.URL;
import com.zjj.dubbo.common.timer.Timeout;
import com.zjj.dubbo.registry.Constants;
import com.zjj.dubbo.registry.NotifyListener;
import com.zjj.dubbo.registry.support.FailbackRegistry;

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
