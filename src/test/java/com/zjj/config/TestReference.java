package com.zjj.config;

import com.zjj.demo.HelloService;
import com.zjj.demo.HelloService2;

import java.lang.reflect.Field;
import java.util.Collections;

public class TestReference {
    private HelloService2 helloService2;

    public void setHelloService2(HelloService2 helloService2) {
        this.helloService2 = helloService2;
    }

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
        methodConfig.setName("compute");

        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setDefault(true);
        protocolConfig.setName("jrpc");

        ReferenceConfig<HelloService2> referenceConfig = new ReferenceConfig<>();

        referenceConfig.setRegistryConfigs(Collections.singletonList(registryConfig));
        referenceConfig.setInterfaceClass(HelloService2.class);
        referenceConfig.setMethodConfigs(Collections.singletonList(methodConfig));
        referenceConfig.setProtocolConfigs(Collections.singletonList(protocolConfig));
        Object ref = referenceConfig.getRef();

        TestReference testReference = new TestReference();


        Field field = testReference.getClass().getDeclaredField("helloService2");
        field.setAccessible(true);
        field.set(testReference, ref);
        long start = System.currentTimeMillis();
        System.out.println(testReference.helloService2.compute("world"));
        long stop = System.currentTimeMillis();
        System.out.println((stop - start) + "ms");

        ReferenceConfig<HelloService> referenceConfig1 = new ReferenceConfig<>();

        referenceConfig1.setRegistryConfigs(Collections.singletonList(registryConfig));
        referenceConfig1.setInterfaceClass(HelloService.class);
        referenceConfig1.setMethodConfigs(Collections.singletonList(methodConfig));
        referenceConfig1.setProtocolConfigs(Collections.singletonList(protocolConfig));
        Object ref1 = referenceConfig1.getRef();
        Field field1 = testReference.getClass().getDeclaredField("helloService");
        field1.setAccessible(true);
        field1.set(testReference, ref1);
        long start1 = System.currentTimeMillis();
        System.out.println(testReference.helloService.compute("world1"));
        long stop1 = System.currentTimeMillis();
        System.out.println((stop1 - start1) + "ms");
    }
}
