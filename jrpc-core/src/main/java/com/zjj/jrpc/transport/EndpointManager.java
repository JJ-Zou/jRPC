package com.zjj.jrpc.transport;

public interface EndpointManager {
    void init();

    void destroy();

    void addEndPoint(Endpoint endPoint);

    void removeEndPoint(Endpoint endPoint);
}
