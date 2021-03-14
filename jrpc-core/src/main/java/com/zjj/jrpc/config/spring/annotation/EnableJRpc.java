package com.zjj.jrpc.config.spring.annotation;


import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@JRpcComponent
public @interface EnableJRpc {
}
