package com.dirkfw.annotation;

import java.lang.annotation.*;
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    String name() default "";
    boolean isRequired() default true;
}
