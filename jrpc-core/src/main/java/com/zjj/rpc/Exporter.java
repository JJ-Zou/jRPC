package com.zjj.rpc;

public interface Exporter<T> extends Node {
    Provider<T> getProvider();

    void unExport();
}
