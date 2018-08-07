package com.enjoy.james.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)//作用范围，注解可以使用在类上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnjoyService {
    String value() default "";//设置一个默认值为“”的value
}
