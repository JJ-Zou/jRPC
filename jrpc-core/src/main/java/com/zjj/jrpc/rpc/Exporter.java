package com.zjj.jrpc.rpc;

public interface Exporter<T> extends Node {
    Provider<T> getProvider();

    void unExport();
}
