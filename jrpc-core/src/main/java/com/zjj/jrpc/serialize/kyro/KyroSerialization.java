package com.zjj.jrpc.serialize.kyro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.zjj.jrpc.serialize.Serialization;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
public class KyroSerialization implements Serialization {
    @Override
    public byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Kryo kryo = new Kryo();
        Output output = new Output(outputStream);
        kryo.writeClassAndObject(output, o);
        return outputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        Kryo kryo = new Kryo();
        Input input = new Input(inputStream);
        return kryo.readObject(input, clazz);
    }

    @Override
    public byte[] serialize(Object[] objects) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Kryo kryo = new Kryo();
        Output output = new Output(outputStream);
        for (Object object : objects) {
            kryo.writeClassAndObject(output, object);
        }
        return outputStream.toByteArray();
    }

    @Override
    public Object[] deserialize(byte[] bytes, List<Class<?>> classes) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        Kryo kryo = new Kryo();
        Input input = new Input(inputStream);
        return classes.stream()
                .map(clazz -> kryo.readObject(input, clazz)).toArray();
    }

    @Override
    public int getSerializeNumber() {
        return 1;
    }
}
