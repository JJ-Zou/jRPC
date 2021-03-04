package com.zjj.rpc.registry.retry;

import com.zjj.dubbo.common.URL;
import com.zjj.dubbo.common.timer.Timeout;
import com.zjj.dubbo.registry.Constants;
import com.zjj.dubbo.registry.support.FailbackRegistry;

public class FailedRegisteredTask extends AbstractRetryTask {
    public FailedRegisteredTask(URL url, FailbackRegistry registry) {
        super(url, registry, Constants.RETRY_REGISTER_NAME);
    }

    @Override
    protected void doRetry(URL url, FailbackRegistry registry, Timeout timeout) {
        registry.doRegister(url);
        registry.removeFailedRegisteredTask(url);
    }
}
