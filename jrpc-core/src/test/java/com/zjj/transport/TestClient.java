package com.zjj.transport;

import com.zjj.HelloService;
import com.zjj.codec.Codec;
import com.zjj.common.utils.ReflectUtils;
import com.zjj.common.utils.RequestIdUtils;
import com.zjj.extension.ExtensionLoader;
import com.zjj.rpc.message.DefaultRequest;
import com.zjj.serialize.Serialization;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class TestClient {

    @Test
    public void encode() throws NoSuchMethodException, IOException {
        Codec codec = ExtensionLoader.getExtensionLoader(Codec.class).getDefaultExtension();
        Method method = HelloService.class.getMethod("hello", String.class);
        DefaultRequest request = DefaultRequest.builder()
                .interfaceName(HelloService.class.getName())
                .methodName(method.getName())
                .parameterSign(ReflectUtils.getParamSigns(method))
                .arguments(new Object[]{"world"})
                .attachments(new HashMap<>())
                .requestId(RequestIdUtils.getRequestId())
                .build();
        long start = System.currentTimeMillis();
        byte[] encode = codec.encode(request);
        Object decode = codec.decode(encode);
        System.out.println(decode);
        long stop = System.currentTimeMillis();
        System.out.println("encode consume: " + (stop - start) + " ms");
    }

    @Test
    public void seri() throws IOException {
        Serialization serialization = ExtensionLoader.getExtensionLoader(Serialization.class).getDefaultExtension();
        long start = System.currentTimeMillis();
        serialization.serialize("world");
        long stop = System.currentTimeMillis();
        System.out.println("serialize consume: " + (stop - start) + " ms");
    }
}
