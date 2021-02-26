package com.zjj.rpc.rpc;

public interface Protocol {
    <T> Exporter<T> export() throws RpcException;
}
