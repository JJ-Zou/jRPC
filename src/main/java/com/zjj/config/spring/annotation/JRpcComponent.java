package com.zjj.config.spring.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JRpcComponent {
    String[] value() default {};
}
