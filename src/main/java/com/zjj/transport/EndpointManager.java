package com.zjj.transport;

public interface EndpointManager {
    void init();

    void destroy();

    void addEndPoint(Endpoint endPoint);

    void removeEndPoint(Endpoint endPoint);
}
