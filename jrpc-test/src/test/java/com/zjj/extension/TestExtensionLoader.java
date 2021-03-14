package com.zjj.extension;

import zjj.codec.Codec;
import zjj.serialize.Serialization;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class TestExtensionLoader {

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(8, 8, 10, TimeUnit.MINUTES,
            new LinkedTransferQueue<>());

    @Test
    public void loadArrayList() {
        List array = ExtensionLoader.getExtensionLoader(List.class).getExtension("array");
        System.out.println(array);
    }

    @Test
    public void concurrent() throws InterruptedException {
        Set<Codec> codecs = ConcurrentHashMap.newKeySet();
        int counts = 200;
        CountDownLatch countDownLatch = new CountDownLatch(counts);
        IntStream.range(0, counts).forEach(i -> EXECUTOR.execute(() -> {
            Codec codec = ExtensionLoader.getExtensionLoader(Codec.class).getDefaultExtension();
            Serialization serialization = ExtensionLoader.getExtensionLoader(Serialization.class).getDefaultExtension();
            codecs.add(codec);
            countDownLatch.countDown();
        }));

        countDownLatch.await();
        System.out.println(codecs);
    }

    @Test
    public void computeIf() throws InterruptedException {
        ConcurrentMap<String, String> map = new ConcurrentHashMap<>();
        int counts = 20;
        CountDownLatch countDownLatch = new CountDownLatch(counts);
        IntStream.range(0, counts).forEach(i -> new Thread(() -> {
            System.out.println(map.computeIfAbsent("a", s -> i + "b"));
            countDownLatch.countDown();
        }).start());

        countDownLatch.await();
        System.out.println(map);
    }
}
