package com.zjj.jrpc.rpc;

import java.util.concurrent.Future;

public interface JFuture<V> extends Future<V> {

    boolean cancel();

    Exception getException();

    boolean isSuccess();

    void addListener(FutureListener<? extends JFuture<? super V>> listener);
}
