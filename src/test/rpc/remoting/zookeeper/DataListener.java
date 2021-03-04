package com.zjj.rpc.remoting.zookeeper;

public interface DataListener {
    void dataChanged(String path, Object value, EventType eventType);
}
