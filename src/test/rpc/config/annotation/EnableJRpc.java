package com.zjj.rpc.config.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@JRpcComponentScan
public @interface EnableJRpc {
}
