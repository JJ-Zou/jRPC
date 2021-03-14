package com.zjj.jrpc.registry.zookeeper;

public enum ZkNodeType {
    AVAILABLE_SERVICE("service"),
    UNAVAILABLE_SERVICE("unavailableService"),
    CLIENT("client");

    private final String value;

    ZkNodeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
