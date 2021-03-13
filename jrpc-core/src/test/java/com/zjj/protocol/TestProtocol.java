package com.zjj.protocol;

import com.zjj.HelloService;
import com.zjj.HelloServiceImpl;
import com.zjj.common.JRpcURL;
import com.zjj.common.JRpcURLParamType;
import com.zjj.protocol.support.DefaultProtocol;
import com.zjj.rpc.Provider;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import com.zjj.rpc.support.DefaultProvider;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TestProtocol {

    public static void main(String[] args) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("address", "39.105.65.104:2181");
        parameters.put(JRpcURLParamType.REGISTRY_RETRY_PERIOD.getName(), "1000");
        JRpcURL jRpcURL = new JRpcURL("jrpc", "127.0.0.1", 20855, "com.zjj.HelloService", parameters);

        DefaultProtocol protocol = new DefaultProtocol();
        final HelloServiceImpl helloService = new HelloServiceImpl();
        protocol.export(new DefaultProvider<>(HelloService.class, helloService, jRpcURL), jRpcURL);
    }
    @Test
    public void testDefaultProtocol() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("address", "39.105.65.104:2181");
        parameters.put(JRpcURLParamType.REGISTRY_RETRY_PERIOD.getName(), "1000");
        JRpcURL jRpcURL = new JRpcURL("jrpc", "127.0.0.1", 20855, "com.zjj.registry.zookeeper", parameters);

        DefaultProtocol protocol = new DefaultProtocol();
        protocol.export(new Provider<String>() {
            @Override
            public Method lookupMethod(String methodName, String methodParameterSign) {
                return null;
            }

            @Override
            public String getImpl() {
                return "dasdsd";
            }

            @Override
            public Class<String> getInterface() {
                return String.class;
            }

            @Override
            public Response call(Request request) {
                return null;
            }

            @Override
            public void init() {

            }

            @Override
            public void destroy() {

            }

            @Override
            public boolean isAvailable() {
                return false;
            }

            @Override
            public String desc() {
                return null;
            }

            @Override
            public JRpcURL getUrl() {
                return jRpcURL;
            }
        }, jRpcURL);
    }
}
