package com.zjj.transport;

public interface EndPointManager {
    void init();

    void destroy();

    void addEndPoint(EndPoint endPoint);

    void removeEndPoint(EndPoint endPoint);
}
