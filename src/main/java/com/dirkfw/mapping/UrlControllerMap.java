package com.dirkfw.mapping;

import java.lang.reflect.Method;

import com.dirkfw.container.ControllerProvider;

public class UrlControllerMap {
    private final Method reflectMethod;
    private final Class<?> controllerClass;
    

    public UrlControllerMap(Method reflectMethod, Class<?> controllerClass,ControllerProvider provider) throws Exception {
        this.reflectMethod = reflectMethod;
        this.controllerClass = controllerClass;
    }

    public Class<?> getControllerClass() { return controllerClass; }

    public Method getReflectMethod() { return reflectMethod; }

  
}