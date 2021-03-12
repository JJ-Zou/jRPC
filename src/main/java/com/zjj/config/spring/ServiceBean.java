package com.zjj.config.spring;

import com.zjj.common.JRpcURLParamType;
import com.zjj.config.ProtocolConfig;
import com.zjj.config.RegistryConfig;
import com.zjj.config.ServiceConfig;
import com.zjj.config.support.ConfigBeanManager;
import com.zjj.exception.JRpcErrorMessage;
import com.zjj.exception.JRpcFrameworkException;
import com.zjj.executor.StandardThreadPoolExecutor;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
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
        Collection<RegistryConfig> registryConfigs = ConfigBeanManager.getRegistryConfigs();
        Collection<ProtocolConfig> protocolConfigs = ConfigBeanManager.getProtocolConfigs();
        setRegistryConfigs(new ArrayList<>(registryConfigs));
        setProtocolConfigs(new ArrayList<>(protocolConfigs));
        if (isDefault() || StringUtils.isEmpty(exportProtocol)) {
            exportProtocol = protocolConfigs.stream()
                    .findAny().orElseThrow(() -> new JRpcFrameworkException("no available ProtocolConfig", JRpcErrorMessage.FRAMEWORK_INIT_ERROR))
                    .getId() + JRpcURLParamType.colon.getValue() + JRpcURLParamType.exportPort.getIntValue();
        }
        log.info("[afterSingletonsInstantiated]: {}", this);
        EXECUTOR.execute(this::export);
    }
}
