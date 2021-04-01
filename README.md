# jRPC
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/JJ-Zou/jRPC/tree/master/LICENSE)

# Quick Start
> The minimum requirements to run the quick start are:
>
> - JDK 1.8 or above
> - A maven project 
> - Zookeeper service

1. Import the project

```shell script
git clone git@github.com:JJ-Zou/jRPC.git
```

2. Create an interface for both service provider and consumer.

```java
public interface HelloService {
    String hello(String name);
}
```

3. Use @Bean to add configurations, and write an implementation with annotation.Start springboot project to export provider service.

```java
@EnableJRpc
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

    @Bean("protocol")
    public ProtocolConfig protocol1() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setProtocolName("rpc");
        protocolConfig.setPort(23250);
        return protocolConfig;
    }

    @Bean("registry")
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setRegisterProtocol("zookeeper");
        registryConfig.setAddress("127.0.0.1:2181");
        return registryConfig;
    }
}
```

```java
@JRpcService(exportProtocol = "protocol", registry = "registry")
public class HelloServiceImpl implements HelloService {
    public String hello(String name) {
        System.out.println("Hello " + name);
    }   
}
```

4. Create and start comsumer project.

```java
@EnableJRpc
@SpringBootApplication
public class ConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @Bean("protocol")
    public ProtocolConfig protocol1() {
        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setProtocolName("rpc");
        protocolConfig.setPort(23250);
        return protocolConfig;
    }

    @Bean("registry")
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setRegisterProtocol("zookeeper");
        registryConfig.setAddress("127.0.0.1:2181");
        return registryConfig;
    }
}
```

```java
@RestController
public class HelloController {

    @JRpcReference(protocol = "protocol", registry = "registry")
    private HelloService helloService;

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name") String name) {
        return helloService.hello(name);
    }
}
```