package com.zjj.transport;

import com.zjj.HelloService;
import com.zjj.codec.Codec;
import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.common.utils.ReflectUtils;
import com.zjj.common.utils.RequestIdUtils;
import com.zjj.extension.ExtensionLoader;
import com.zjj.rpc.ResponseFuture;
import com.zjj.rpc.message.DefaultRequest;
import com.zjj.transport.netty.client.NettyClient;
import com.zjj.transport.netty.server.NettyEndpointFactory;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class TestNetty {

    public static void main(String[] args) throws NoSuchMethodException, IOException, ExecutionException, InterruptedException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("address", "39.105.65.104:2181");
        parameters.put(JRpcURLParamType.registryRetryPeriod.getName(), "1000");
        JRpcURL jRpcURL = new JRpcURL("jrpc", "127.0.0.1", 20855, "com.zjj.registry.zookeeper", parameters);

        NettyClient nettyClient = new NettyClient(jRpcURL);
        nettyClient.open();
        Method method = HelloService.class.getMethod("hello", String.class);
        IntStream.range(0, 10).forEach(i -> {
            new Thread(() -> {
                long start = 0;
                ResponseFuture responseFuture = null;
                try {
                    DefaultRequest request = DefaultRequest.builder()
                            .interfaceName(HelloService.class.getName())
                            .methodName(method.getName())
                            .parameterSign(ReflectUtils.getParamSigns(method))
                            .arguments(new Object[]{"world"})
                            .attachments(new HashMap<>())
                            .requestId(RequestIdUtils.getRequestId())
                            .build();
                    start = System.currentTimeMillis();
                    responseFuture = (ResponseFuture) nettyClient.request(request);
                    System.out.println(responseFuture.getRequestId() + " call: " + responseFuture.get());
                } catch (IOException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                System.out.println(responseFuture.getRequestId() + " consume: " + (System.currentTimeMillis() - start) + "ms");
            }).start();
        });
    }

    @Test
    public void nettyFactory() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("address", "39.105.65.104:2181");
        parameters.put(JRpcURLParamType.registryRetryPeriod.getName(), "1000");
        JRpcURL jRpcURL = new JRpcURL("jrpc", "127.0.0.1", 20855, "com.zjj.registry.zookeeper", parameters);
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
