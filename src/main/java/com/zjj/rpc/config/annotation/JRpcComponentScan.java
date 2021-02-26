package com.zjj.rpc.config.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(JRpcComponentScanRegistrar.class)
public @interface JRpcComponentScan {
    /**
     * basePackages的别名
     *
     * @return basePackages
     */
    String[] value() default {};

    /**
     * 被扫描的包名
     *
     * @return basePackages
     */
    String[] basePackages() default {};

    /**
     * 为basePackages指定类型
     *
     * @return basePackages
     */
    Class<?>[] basePackageClasses() default {};
}
