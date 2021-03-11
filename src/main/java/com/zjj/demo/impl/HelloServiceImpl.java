package com.zjj.demo.impl;

import com.zjj.config.spring.annotation.JRpcService;
import com.zjj.demo.HelloService;

@JRpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String compute(String input) {
        return "hello " + input;
    }
}
