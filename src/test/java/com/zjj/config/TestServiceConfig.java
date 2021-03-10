package com.zjj.config;

import com.zjj.HelloService;
import com.zjj.HelloServiceImpl;
import org.junit.Test;

import java.util.Collections;

public class TestServiceConfig {
    public static void main(String[] args) {
        HelloServiceImpl helloService = new HelloServiceImpl();

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setRegisterProtocol("zookeeper");
        registryConfig.setAddress("39.105.65.104");
        registryConfig.setPort(2181);

        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setName("hello");

        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setId("protocolConfig");

        ServiceConfig<HelloService> serviceConfig = new ServiceConfig<>();
//        serviceConfig.setExportHost("10.135.21.93");
        serviceConfig.setExportProtocol("protocolConfig:15520");
        serviceConfig.setRegistryConfigs(Collections.singletonList(registryConfig));
        serviceConfig.setInterfaceClass(HelloService.class);
        serviceConfig.setRef(helloService);
        serviceConfig.setProtocolConfigs(Collections.singletonList(protocolConfig));
//        serviceConfig.setExportHost("39.105.65.104");
        serviceConfig.export();
    }
    @Test
    public void serviceStart() {

    }
}
