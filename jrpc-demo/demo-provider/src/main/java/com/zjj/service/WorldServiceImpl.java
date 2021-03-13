package com.zjj.service;

//@Service
//@JRpcService(exportProtocol = "protocol_1:18250", registry = "registry_1", exportHost = "39.105.65.104")
public class WorldServiceImpl implements WorldService {
    @Override
    public String world(String hello) {
        return hello + " world";
    }
}
