package com.zjj.jrpc.rpc;


import java.util.EventListener;

public interface FutureListener<F extends JFuture<?>> extends EventListener {

    void operationComplete(F future);

}
