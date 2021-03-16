package com.zjj.jrpc.controller;

import com.zjj.jrpc.config.spring.annotation.JRpcReference;
import com.zjj.jrpc.service.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @JRpcReference(protocol = "protocol_1", registry = "registry_1")
    private HelloService helloService;

    @GetMapping("/")
    public String hello() {
        return helloService.hello("world");
    }
}
