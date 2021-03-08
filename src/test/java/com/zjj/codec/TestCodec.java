package com.zjj.codec;

import com.zjj.common.utils.ReflectUtils;
import com.zjj.common.utils.RequestIdUtils;
import com.zjj.extension.ExtensionLoader;
import com.zjj.rpc.Request;
import com.zjj.rpc.Response;
import com.zjj.rpc.message.DefaultRequest;
import com.zjj.serialize.Serialization;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class TestCodec {
    @Test
    public void codec() {
        Class clazz = Codec.class;
        System.out.println(clazz);
        System.out.println(DefaultCodec.class);
        Codec codec = ExtensionLoader.getExtensionLoader(Codec.class).getDefaultExtension();
        System.out.println(codec.getClass());
    }

    @Test
    public void defaultCodec() throws IOException, NoSuchMethodException {
        Codec codec = ExtensionLoader.getExtensionLoader(Codec.class).getDefaultExtension();
        Method method = Response.class.getMethod("setProcessTime", long.class);
        DefaultRequest request = DefaultRequest.builder()
                .interfaceName(Codec.class.getName())
                .methodName(method.getName())
                .parameterSign(ReflectUtils.getParamSigns(method))
                .arguments(new Object[]{321312L})
                .attachments(new HashMap<>())
                .requestId(RequestIdUtils.getRequestId())
                .build();
        byte[] encode = codec.encode(request);
        DefaultRequest decode = (DefaultRequest) codec.decode(encode);
        System.out.println(decode);
    }

    @Test
    public void testLong() throws IOException, ClassNotFoundException {
        Serialization serialization = ExtensionLoader.getExtensionLoader(Serialization.class).getDefaultExtension();
        System.out.println(long.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = new ObjectOutputStream(outputStream);
        objectOutput.writeObject(serialization.serialize(1));
        objectOutput.writeInt(0);
        objectOutput.flush();
        byte[] bytes = outputStream.toByteArray();
        System.out.println(Arrays.toString(bytes));
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInput objectInput = new ObjectInputStream(inputStream);
        Object o = objectInput.readObject();
        System.out.println(serialization.deserialize((byte[]) o, long.class));
    }
}
