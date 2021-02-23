package com.zjj.rpc;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ApplicationModel {
    private static final String NAME = "application";

    private static AtomicBoolean INIT_FLAG = new AtomicBoolean(false);

    public static void init() {
        if (INIT_FLAG.compareAndSet(false, true)) {

        }
    }

}
