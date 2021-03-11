package com.zjj.demo.impl;

import com.zjj.config.spring.annotation.JRpcService;
import com.zjj.demo.HelloService2;

@JRpcService
public class HelloServiceImpl2 implements HelloService2 {
    @Override
    public String compute(String input) {
        return "hello " + input;
    }
}
