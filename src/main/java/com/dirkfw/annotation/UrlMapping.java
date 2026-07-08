package com.dirkfw.annotation;
import java.lang.annotation.*;

import com.dirkfw.classes.helper.UrlHTTPMethod;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlMapping {
    String value();
    UrlHTTPMethod httpMethod() default UrlHTTPMethod.GET;
}
