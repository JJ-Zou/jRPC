package com.zjj.jrpc.config.spring;

import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.config.ServiceConfig;
import com.zjj.jrpc.executor.StandardThreadPoolExecutor;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class ServiceBean<T> extends ServiceConfig<T> implements SmartInitializingSingleton {
    private static final long serialVersionUID = -5173125703434531081L;

    private static final ThreadPoolExecutor EXECUTOR = new StandardThreadPoolExecutor(
            JRpcURLParamType.CORE_POOL_SIZE.getIntValue(),
            JRpcURLParamType.CORE_POOL_SIZE.getIntValue(),
            JRpcURLParamType.WORKER_QUEUE_SIZE.getIntValue(),
            new DefaultThreadFactory("ServiceBean-pool-", true)
    );

    @Override
    public void afterSingletonsInstantiated() {
        log.info("[afterSingletonsInstantiated]: {}", this);
        if (isExport()) {
            EXECUTOR.execute(this::export);
        }
    }
}
