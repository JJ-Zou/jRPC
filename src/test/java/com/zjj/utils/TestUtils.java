package com.zjj.utils;

import com.zjj.common.utils.ReflectUtils;
import com.zjj.exception.AbstractJRpcException;
import com.zjj.exception.JRpcFrameworkException;
import com.zjj.transport.EndpointFactory;
import org.junit.Test;

import java.lang.reflect.Method;

public class TestUtils {
    @Test
    public void getMethodSign() {
        for (Method method : EndpointFactory.class.getMethods()) {
            if (method.getDeclaringClass() == EndpointFactory.class) {
                System.out.println(ReflectUtils.getMethodSign(method));
            }
        }
    }

    @Test
    public void isAssignFrom() {
        System.out.println(AbstractJRpcException.class.isAssignableFrom(JRpcFrameworkException.class));
        System.out.println(JRpcFrameworkException.class.isAssignableFrom(AbstractJRpcException.class));
        System.out.println(JRpcFrameworkException.class.isAssignableFrom(RuntimeException.class));
        System.out.println(RuntimeException.class.isAssignableFrom(JRpcFrameworkException.class));
    }
}
