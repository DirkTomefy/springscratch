package com.dirkfw.mapping;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

import com.dirkfw.container.BeanProvider;

public class UrlControllerMap {
    private final Method reflectMethod;
    private final Class<?> controllerClass;
    private final BeanProvider beanProvider;
    

    public UrlControllerMap(Method reflectMethod, Class<?> controllerClass,BeanProvider provider) throws Exception {
        this.reflectMethod = reflectMethod;
        this.controllerClass = controllerClass;
        this.beanProvider=provider;
    }

    public Object getControllerInstance(HttpServletRequest request) throws ReflectiveOperationException {
        return beanProvider.getBean(controllerClass);
    }

    public Method getReflectMethod() { return reflectMethod; }

  
}