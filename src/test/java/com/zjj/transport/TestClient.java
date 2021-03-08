package com.zjj.transport;

import com.zjj.HelloService;
import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.common.utils.ReflectUtils;
import com.zjj.common.utils.RequestIdUtils;
import com.zjj.rpc.ResponseFuture;
import com.zjj.rpc.message.DefaultRequest;
import com.zjj.transport.netty.client.NettyClient;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class TestClient {
    public static void main(String[] args) throws NoSuchMethodException, IOException, ExecutionException, InterruptedException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("address", "39.105.65.104:2181");
        parameters.put(JRpcURLParamType.registryRetryPeriod.getName(), "1000");
        JRpcURL jRpcURL = new JRpcURL("jrpc", "127.0.0.1", 20855, "com.zjj.registry.zookeeper", parameters);

        NettyClient nettyClient = new NettyClient(jRpcURL);
        nettyClient.open();
        Method method = HelloService.class.getMethod("hello", String.class);
        DefaultRequest request = DefaultRequest.builder()
                .interfaceName(HelloService.class.getName())
                .methodName(method.getName())
                .parameterSign(ReflectUtils.getParamSigns(method))
                .arguments(new Object[]{"world"})
                .attachments(new HashMap<>())
                .requestId(RequestIdUtils.getRequestId())
                .build();
        ResponseFuture responseFuture = (ResponseFuture) nettyClient.request(request);
        IntStream.range(0, 20).forEach(i -> {
            new Thread(() -> {
                try {
                    System.out.println("dasdsa");
                    System.out.println(responseFuture.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }).start();
        });


    }
}
