package com.dirkfw.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionRequired {

    String[] value() default {}; //! roles autorises ; vide = pas de controle de role. Nom de la variable de role : dirk.authrole du propreties

    Class<?> type() default Object.class;
}