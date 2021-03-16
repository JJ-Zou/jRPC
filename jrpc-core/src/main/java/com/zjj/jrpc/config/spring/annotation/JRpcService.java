package com.zjj.jrpc.config.spring.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JRpcService {

    Class<?> interfaceClass() default void.class;

    String interfaceName() default "";

    String version() default "";

    String group() default "";

    String path() default "";

    String application() default "";

    String module() default "";

    String exportProtocol() default "";

    String exportHost() default "";

    int exportPort() default 0;

    String registry() default "";

    boolean export() default true;
}
