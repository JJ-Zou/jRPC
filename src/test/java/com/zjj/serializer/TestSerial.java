package com.zjj.serializer;

import com.zjj.extension.ExtensionLoader;
import com.zjj.serialize.Serialization;
import com.zjj.serialize.hessian2.Hessian2Serialization;
import org.checkerframework.checker.units.qual.C;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.Executors;

public class TestSerial {

    @Test
    public void test() throws IOException {
        String[] strings = new String[4];
        for (int i = 0; i < 4; i++) {
            strings[i] = String.valueOf(System.currentTimeMillis());
        }
        System.out.println(Arrays.toString(strings));
        Hessian2Serialization serialization = new Hessian2Serialization();
        byte[] serialize = serialization.serialize(strings);
        System.out.println(serialize);
        Class[] classes = new Class[4];
        Arrays.fill(classes, String.class);
        Object[] deserialize = serialization.deserialize(serialize, Arrays.asList(classes));
        System.out.println(Arrays.toString(deserialize));
    }

    @Test
    public void serial() {
        Class clazz = Serialization.class;
        System.out.println(clazz);
        System.out.println(Hessian2Serialization.class);
        Serialization extension = ExtensionLoader.getExtensionLoader(Serialization.class).getDefaultExtension();
        System.out.println(extension.getClass());
    }
}
