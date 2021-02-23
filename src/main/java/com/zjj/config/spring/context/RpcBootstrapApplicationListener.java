package com.zjj.config.spring.context;

import com.zjj.config.bootstrap.RpcBootstrap;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class RpcBootstrapApplicationListener extends OneTimeExecutionApplicationContextEventListener implements Ordered {
    public static final String BEAN_NAME = "rpcBootstrapApplicationListener";

    private final RpcBootstrap rpcBootstrap;

    public RpcBootstrapApplicationListener() {
        this.rpcBootstrap = RpcBootstrap.getInstance();
    }

    @Override
    protected void onApplicationContextEvent(ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            onContextRefreshedEvent();
        } else if (event instanceof ContextStoppedEvent) {
            onContextStoppedEvent();
        }
    }

    private void onContextRefreshedEvent() {
        rpcBootstrap.start();
    }

    private void onContextStoppedEvent() {
        rpcBootstrap.stop();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
