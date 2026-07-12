package com.dirkfw.mapping;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

public class UrlControllerMap {
    private final Method reflectMethod;
    private final Class<?> controllerClass;
    private final Scope scope;
    private Object singletonInstance; 


    public UrlControllerMap(Method reflectMethod, Class<?> controllerClass, Scope scope) throws Exception {
        this.reflectMethod = reflectMethod;
        this.controllerClass = controllerClass;
        this.scope = scope;

        if (scope == Scope.SINGLETON) {
            this.singletonInstance = createNewInstance();
        }
    }

    public Object getControllerInstance(HttpServletRequest request) throws ReflectiveOperationException {
        switch (scope) {
            case SINGLETON:
                return singletonInstance;
            case PROTOTYPE:
                return createNewInstance();
            default:
                throw new IllegalStateException("Scope non supporté : " + scope);
        }
    }

    private Object createNewInstance() throws ReflectiveOperationException {
        return controllerClass.getDeclaredConstructor().newInstance();
    }

    public Method getReflectMethod() { return reflectMethod; }

  
}