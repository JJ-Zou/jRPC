package com.zjj.rpc.config.support;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Parameter {
    String key() default "";

    boolean excluded() default false;
}
