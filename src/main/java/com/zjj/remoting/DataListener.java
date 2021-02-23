package com.zjj.remoting;

public interface DataListener {
    void dataChanged(String path, Object value, EventType eventType);
}
