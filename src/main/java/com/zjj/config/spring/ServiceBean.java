package com.zjj.config.spring;

import com.zjj.common.JRpcURLParamType;
import com.zjj.config.ServiceConfig;
import com.zjj.executor.StandardThreadPoolExecutor;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class ServiceBean<T> extends ServiceConfig<T> implements SmartInitializingSingleton {
    private static final long serialVersionUID = -5173125703434531081L;

    private static final ThreadPoolExecutor EXECUTOR = new StandardThreadPoolExecutor(
            JRpcURLParamType.corePoolSize.getIntValue(),
            JRpcURLParamType.corePoolSize.getIntValue(),
            JRpcURLParamType.workerQueueSize.getIntValue(),
            new DefaultThreadFactory("ServiceBean-pool-", true)
    );

    private String beanName;

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public void afterSingletonsInstantiated() {
        log.info("[afterSingletonsInstantiated]: {}", this);
        if (isExport()) {
            EXECUTOR.execute(this::export);
        }
    }
}
