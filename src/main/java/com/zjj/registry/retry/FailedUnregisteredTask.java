package com.zjj.registry.retry;

import com.zjj.common.URL;
import com.zjj.registry.Constants;
import com.zjj.registry.support.FailbackRegistry;
import com.zjj.common.timer.Timeout;

public class FailedUnregisteredTask extends AbstractRetryTask {
    public FailedUnregisteredTask(URL url, FailbackRegistry registry) {
        super(url, registry, Constants.RETRY_UNREGISTER_NAME);
    }

    @Override
    protected void doRetry(URL url, FailbackRegistry registry, Timeout timeout) {
        registry.doUnregister(url);
        registry.removeFailedUnregisteredTask(url);
    }
}
