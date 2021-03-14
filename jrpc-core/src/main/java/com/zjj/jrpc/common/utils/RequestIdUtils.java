package com.zjj.jrpc.common.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestIdUtils {
    private static final AtomicInteger OFFSET = new AtomicInteger(0);
    private static final int BIT = 20;
    private static final int MASK = 1 << BIT;

    private RequestIdUtils() {
    }

    public static long getRequestId() {
        long currentTime = System.currentTimeMillis();
        int count = OFFSET.incrementAndGet();
        if (count >= MASK) {
            synchronized (RequestIdUtils.class) {
                if (OFFSET.get() >= MASK) {
                    OFFSET.set(0);
                }
            }
            count = OFFSET.incrementAndGet();
        }
        return (currentTime << BIT) + count;
    }
}
