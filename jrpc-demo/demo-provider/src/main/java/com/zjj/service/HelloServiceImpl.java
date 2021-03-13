package com.zjj.service;

import com.zjj.config.spring.annotation.JRpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@JRpcService(exportProtocol = "protocol_1:18250", registry = "registry_1")
public class HelloServiceImpl implements HelloService {

    @JRpcReference(protocol = "protocol_1", registry = "registry_1")
    private WorldService worldService;

    @GetMapping("/test")
    @Override
    public String hello(@RequestParam(value = "input", required = false) String world) {
        return worldService.world(world);
    }
}
