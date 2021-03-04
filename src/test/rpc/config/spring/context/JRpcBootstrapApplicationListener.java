package com.zjj.rpc.config.spring.context;

import com.zjj.rpc.config.bootstrap.JRpcBootstrap;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

public class JRpcBootstrapApplicationListener implements ApplicationListener {

    public static final String BEAN_NAME = "JRpcBootstrapApplicationListener";

    private final JRpcBootstrap bootstrap;

    public JRpcBootstrapApplicationListener() {
        this.bootstrap = JRpcBootstrap.getInstance();
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            onContextRefreshedEvent();
        } else if (event instanceof ContextClosedEvent) {
            onContextClosedEvent();
        }
    }

    private void onContextRefreshedEvent() {
        bootstrap.start();
    }

    private void onContextClosedEvent() {
        bootstrap.stop();
    }

}
