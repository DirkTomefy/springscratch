package com.dirkfw.mapping;

import java.lang.reflect.Method;


public class UrlControllerMap {
    private final Method reflectMethod;
    private final Class<?> controllerClass;
    

    public UrlControllerMap(Method reflectMethod, Class<?> controllerClass) throws Exception {
        this.reflectMethod = reflectMethod;
        this.controllerClass = controllerClass;
    }

    public Class<?> getControllerClass() { return controllerClass; }

    public Method getReflectMethod() { return reflectMethod; }

  
}