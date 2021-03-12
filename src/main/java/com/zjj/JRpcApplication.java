package com.zjj;

import com.zjj.config.ProtocolConfig;
import com.zjj.config.RegistryConfig;
import com.zjj.config.spring.annotation.EnableJRpc;
import com.zjj.config.spring.initializer.JRpcApplicationContextInitializer;
import com.zjj.config.support.ConfigBeanManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableJRpc
public class JRpcApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(JRpcApplication.class);
        springApplication.addInitializers(new JRpcApplicationContextInitializer());
        springApplication.run(args);
    }

    @Bean
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName("jrpc");
        return protocolConfig;
    }

    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setRegisterProtocol("zookeeper");
        registryConfig.setAddress("39.105.65.104");
        registryConfig.setPort(2181);
        return registryConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        ConfigBeanManager.getAbstractConfigMap().entrySet().forEach(System.out::println);
    }
}
