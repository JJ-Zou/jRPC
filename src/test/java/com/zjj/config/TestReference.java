package com.zjj.config;

import com.zjj.HelloService;
import com.zjj.HelloServiceImpl;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

public class TestReference {
    private HelloService helloService;

    public void setHelloService(HelloService helloService) {
        this.helloService = helloService;
    }

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setRegisterProtocol("zookeeper");
        registryConfig.setAddress("39.105.65.104");
        registryConfig.setPort(2181);

        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setName("hello");

        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setDefault(true);
        protocolConfig.setName("jrpc");

        ReferenceConfig<HelloService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setRegistryConfigs(Collections.singletonList(registryConfig));
        referenceConfig.setInterfaceClass(HelloService.class);
        referenceConfig.setMethods(Collections.singletonList(methodConfig));
        referenceConfig.setProtocolConfigs(Collections.singletonList(protocolConfig));
        Object ref = referenceConfig.getRef();

        TestReference testReference = new TestReference();
        Field field = testReference.getClass().getDeclaredField("helloService");
        field.setAccessible(true);
        field.set(testReference, ref);
        long start = System.currentTimeMillis();
        System.out.println(testReference.helloService.hello("world"));
        long stop = System.currentTimeMillis();
        System.out.println((stop - start) + "ms");
    }
}
