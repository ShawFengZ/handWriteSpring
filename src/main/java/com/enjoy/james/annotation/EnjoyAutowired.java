package com.enjoy.james.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})//作用范围，注解可以使用在属性
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnjoyAutowired {
    String value() default "";//设置一个默认值为“”的value
}
