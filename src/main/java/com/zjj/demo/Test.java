package com.zjj.demo;

import com.zjj.config.MethodConfig;
import com.zjj.config.ProtocolConfig;
import com.zjj.config.ReferenceConfig;
import com.zjj.config.RegistryConfig;

import java.lang.reflect.Field;
import java.util.Collections;

public class Test {
    private HelloService2 helloService2;

    public void setHelloService2(HelloService2 helloService2) {
        this.helloService2 = helloService2;
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

        Test testReference = new Test();
        Field field = testReference.getClass().getDeclaredField("helloService2");
        field.setAccessible(true);
        field.set(testReference, ref);
        long start = System.currentTimeMillis();
        System.out.println(testReference.helloService2.compute("world"));
        long stop = System.currentTimeMillis();
        System.out.println((stop - start) + "ms");
    }
}
