package com.zjj.jrpc;

import com.zjj.jrpc.config.ProtocolConfig;
import com.zjj.jrpc.config.RegistryConfig;
import com.zjj.jrpc.config.spring.annotation.EnableJRpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableJRpc
@SpringBootApplication
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @Value("${jrpc.protocolName}")
    private String protocolName;

    @Value("${jrpc.port1}")
    private int port1;

    @Bean("protocol_1")
    public ProtocolConfig protocol1() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setProtocolName(protocolName);
        protocolConfig.setPort(port1);
        return protocolConfig;
    }

    @Value("${jrpc.port2}")
    private int port2;

    @Bean("protocol_2")
    public ProtocolConfig protocol2() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setProtocolName(protocolName);
        protocolConfig.setPort(port2);
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
