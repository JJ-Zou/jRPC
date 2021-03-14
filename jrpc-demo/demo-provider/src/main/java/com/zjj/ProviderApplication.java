package com.zjj;

import com.zjj.jrpc.config.ProtocolConfig;
import com.zjj.jrpc.config.RegistryConfig;
import com.zjj.jrpc.config.spring.annotation.EnableJRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableJRpc
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

    @Bean("protocol_1")
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setProtocolName("jrpc");
        return protocolConfig;
    }

    @Bean("registry_1")
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setRegisterProtocol("zookeeper");
        registryConfig.setAddress("39.105.65.104:2181");
        return registryConfig;
    }
}
