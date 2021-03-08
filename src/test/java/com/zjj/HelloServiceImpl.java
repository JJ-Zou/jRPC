package com.zjj;

public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String str) {
        return "hello " + str;
    }
}
