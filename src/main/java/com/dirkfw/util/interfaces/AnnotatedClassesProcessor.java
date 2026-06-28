package com.dirkfw.util.interfaces;



@FunctionalInterface
public interface AnnotatedClassesProcessor {
    void process(Class<?> clazz) throws Exception;
}