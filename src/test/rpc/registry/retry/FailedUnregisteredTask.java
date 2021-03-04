package com.zjj.rpc.registry.retry;

import com.zjj.dubbo.common.URL;
import com.zjj.dubbo.common.timer.Timeout;
import com.zjj.dubbo.registry.Constants;
import com.zjj.dubbo.registry.support.FailbackRegistry;

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
