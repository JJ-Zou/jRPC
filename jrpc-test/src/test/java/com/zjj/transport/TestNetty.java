package com.zjj.transport;

import com.zjj.HelloService;
import zjj.common.JRpcURL;
import zjj.common.JRpcURLParamType;
import zjj.common.utils.ReflectUtils;
import zjj.common.utils.RequestIdUtils;
import zjj.extension.ExtensionLoader;
import zjj.rpc.Response;
import zjj.rpc.message.DefaultRequest;
import zjj.transport.netty.client.NettyClient;
import zjj.transport.netty.server.NettyEndpointFactory;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class TestNetty {

    public static void main(String[] args) throws NoSuchMethodException, IOException, ExecutionException, InterruptedException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("address", "39.105.65.104:2181");
        parameters.put(JRpcURLParamType.REGISTRY_RETRY_PERIOD.getName(), "1000");
        JRpcURL jRpcURL = new JRpcURL("jrpc", "127.0.0.1", 20855, "zjj.registry.zookeeper", parameters);

        NettyClient nettyClient = new NettyClient(jRpcURL);
        nettyClient.open();
        Method method = HelloService.class.getMethod("hello", String.class);
        long start = 0;
        Response responseFuture = null;
        try {
            DefaultRequest request = DefaultRequest.builder()
                    .interfaceName(HelloService.class.getName())
                    .methodName(method.getName())
                    .parameterSign(ReflectUtils.getParamSigns(method))
                    .arguments(new Object[]{"world " + "single0"})
                    .attachments(new HashMap<>())
                    .requestId(RequestIdUtils.getRequestId())
                    .build();
            start = System.currentTimeMillis();
            responseFuture = nettyClient.request(request);
            System.out.println(responseFuture.getRequestId() + " call: " + responseFuture.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(responseFuture.getRequestId() + " consume: " + (System.currentTimeMillis() - start) + "ms");

        IntStream.range(0, 10).forEach(i -> {
            new Thread(() -> {
                long start1 = 0;
                Response responseFuture1 = null;
                try {
                    DefaultRequest request = DefaultRequest.builder()
                            .interfaceName(HelloService.class.getName())
                            .methodName(method.getName())
                            .parameterSign(ReflectUtils.getParamSigns(method))
                            .arguments(new Object[]{"world " + i})
                            .attachments(new HashMap<>())
                            .requestId(RequestIdUtils.getRequestId())
                            .build();
                    start1 = System.currentTimeMillis();
                    responseFuture1 = nettyClient.request(request);
                    System.out.println(responseFuture1.getRequestId() + " call: " + responseFuture1.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(responseFuture1.getRequestId() + " consume: " + (System.currentTimeMillis() - start1) + "ms");
            }).start();
        });
        TimeUnit.SECONDS.sleep(1);
        start = 0;
        responseFuture = null;
        try {
            DefaultRequest request = DefaultRequest.builder()
                    .interfaceName(HelloService.class.getName())
                    .methodName(method.getName())
                    .parameterSign(ReflectUtils.getParamSigns(method))
                    .arguments(new Object[]{"world " + "single1"})
                    .attachments(new HashMap<>())
                    .requestId(RequestIdUtils.getRequestId())
                    .build();
            start = System.currentTimeMillis();
            responseFuture = nettyClient.request(request);
            System.out.println(responseFuture.getRequestId() + " call: " + responseFuture.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(responseFuture.getRequestId() + " consume: " + (System.currentTimeMillis() - start) + "ms");

    }

    @Test
    public void nettyFactory() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("address", "39.105.65.104:2181");
        parameters.put(JRpcURLParamType.REGISTRY_RETRY_PERIOD.getName(), "1000");
        JRpcURL jRpcURL = new JRpcURL("jrpc", "127.0.0.1", 20855, "zjj.registry.zookeeper", parameters);
        Server server = new NettyEndpointFactory().createServer(jRpcURL, null);
        server.open();
    }

    @Test
    public void heartbeatFactory() {
        System.out.println(HeartBeatFactory.class);
        HeartBeatFactory defaultExtension = ExtensionLoader.getExtensionLoader(HeartBeatFactory.class).getDefaultExtension();
        System.out.println(defaultExtension.getClass());
    }

    @Test
    public void endPointFactory() {
        System.out.println(EndpointFactory.class);
        EndpointFactory defaultExtension = ExtensionLoader.getExtensionLoader(EndpointFactory.class).getDefaultExtension();
        System.out.println(defaultExtension.getClass());
    }
}
