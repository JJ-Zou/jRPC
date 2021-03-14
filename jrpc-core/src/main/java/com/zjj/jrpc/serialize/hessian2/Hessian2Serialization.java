package com.zjj.jrpc.serialize.hessian2;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.zjj.jrpc.serialize.Serialization;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
public class Hessian2Serialization implements Serialization {

    @Override
    public byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(outputStream);
        hessian2Output.writeObject(o);
        hessian2Output.flush();
        return outputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        Hessian2Input hessian2Input = new Hessian2Input(new ByteArrayInputStream(bytes));
        return (T) hessian2Input.readObject(clazz);
    }

    @Override
    public byte[] serialize(Object[] objects) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(outputStream);
        for (Object o : objects) {
            hessian2Output.writeObject(o);
        }
        hessian2Output.flush();
        return outputStream.toByteArray();
    }

    @Override
    public Object[] deserialize(byte[] bytes, List<Class<?>> classes) throws IOException {
        Hessian2Input hessian2Input = new Hessian2Input(new ByteArrayInputStream(bytes));
        return classes.stream()
                .map(clazz -> {
                    try {
                        return hessian2Input.readObject(clazz);
                    } catch (IOException e) {
                        log.error("deserialize {} error, default return null.", clazz, e);
                        return null;
                    }
                }).toArray();
    }

    @Override
    public int getSerializeNumber() {
        return 0;
    }
}
