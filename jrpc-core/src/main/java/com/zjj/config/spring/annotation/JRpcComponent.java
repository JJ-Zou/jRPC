package com.zjj.config.spring.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(JRpcImportRegistrar.class)
public @interface JRpcComponent {
    String[] value() default {};
}
