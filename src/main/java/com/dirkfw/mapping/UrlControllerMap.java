package com.dirkfw.mapping;

import java.lang.reflect.Method;

public class UrlControllerMap {

    Method reflectMethod;
    Class<?> controllerClasses;
    Object seedSingleton;
   
    public UrlControllerMap(Method reflectMethod, Class<?> controllerClasses) {
        this.reflectMethod = reflectMethod;
        this.controllerClasses = controllerClasses;
        
        try {
            this.seedSingleton = controllerClasses.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Erreur lors de la création du singleton du contrôleur : " + controllerClasses.getName(), e);
        }
    }

    public Class<?> getControllerClasses() {
        return controllerClasses;
    }

    public void setControllerClasses(Class<?> controllerClasses) {
        this.controllerClasses = controllerClasses;
    }

     public Method getReflectMethod() {
        return reflectMethod;
    }

    public void setReflectMethod(Method reflectMethod) {
        this.reflectMethod = reflectMethod;
    }
    
    @Override
    public String toString() {
        return "UrlControllerMap [method=" + reflectMethod + ", controllerClasses=" + controllerClasses + "]";
    }

    public Object getSeedSingleton() {
        return seedSingleton;
    }

    public void setSeedSingleton(Object seedSingleton) {
        this.seedSingleton = seedSingleton;
    }

}
