package com.zjj.serialize;

import java.io.IOException;
import java.util.List;

public interface Serialization {
    byte[] serialize(Object o) throws IOException;

    <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException;

    byte[] serialize(Object[] objects) throws IOException;

    Object[] deserialize(byte[] bytes, List<Class<?>> classes) throws IOException;

    int getSerializeNumber();
}
