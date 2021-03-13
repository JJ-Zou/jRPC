package com.zjj.config.spring.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JRpcReference {

    String id() default "";

    Class<?> interfaceClass() default void.class;

    String interfaceName() default "";

    String directAddress() default "";

    String application() default "";

    String module() default "";

    String version() default "1.0";

    String group() default "default_rpc";

    String protocol() default "";

    String registry() default "";

}
