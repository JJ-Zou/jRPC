package com.zjj.config.spring.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JRpcService {

    Class<?> interfaceClass() default void.class;

    String interfaceName() default "";

    String version() default "1.0";

    String group() default "default_rpc";

    String path() default "";

    boolean export() default true;
}
