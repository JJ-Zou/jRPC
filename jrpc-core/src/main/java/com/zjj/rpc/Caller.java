package com.zjj.rpc;


public interface Caller<T> extends Node {

    Class<T> getInterface();

    Response call(Request request);
}
