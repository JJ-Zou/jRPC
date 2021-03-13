package com.zjj;

import com.zjj.common.utils.ReflectUtils;
import com.zjj.config.MethodConfig;
import com.zjj.config.ProtocolConfig;
import com.zjj.config.ReferenceConfig;
import com.zjj.config.RegistryConfig;
import com.zjj.config.spring.annotation.JRpcReference;
import com.zjj.config.spring.annotation.JRpcService;
import com.zjj.service.HelloService;
import jdk.nashorn.internal.ir.annotations.Reference;

import java.lang.reflect.Field;
import java.util.Collections;

public class ConsumerApplication {

    @JRpcReference
    private HelloService helloService;

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        ConsumerApplication application = new ConsumerApplication();

        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setProtocolName("jrpc");

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setRegisterProtocol("zookeeper");
        registryConfig.setAddress("39.105.65.104:2181");

        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setName("hello");

        ReferenceConfig<HelloService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterfaceClass(HelloService.class);
        referenceConfig.setMethodConfigs(Collections.singletonList(methodConfig));
        referenceConfig.setRegistryConfigs(Collections.singletonList(registryConfig));
        referenceConfig.setProtocolConfigs(Collections.singletonList(protocolConfig));

        Object ref = referenceConfig.getRef();

        Field field = application.getClass().getDeclaredField("helloService");
        ReflectUtils.makeAccessible(field);
        field.set(application, ref);
        long start = System.currentTimeMillis();
        String ret = application.helloService.hello("world");
        long stop = System.currentTimeMillis();

        System.out.println("return: " + ret + ", consume: " + (stop - start) + "ms");
    }
}
