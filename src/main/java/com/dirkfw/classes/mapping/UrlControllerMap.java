package com.dirkfw.classes.mapping;

import java.lang.reflect.Method;

public class UrlControllerMap {

    Method reflectMethod;
    Class<?> controllerClasses;
   
    public UrlControllerMap(Method reflectMethod, Class<?> controllerClasses) {
        this.reflectMethod = reflectMethod;
        this.controllerClasses = controllerClasses;
        
      
    }

    public Class<?> getControllerClasses() {
        return controllerClasses;
    }

    public Object getPrototypeSeed() throws ReflectiveOperationException{
            return controllerClasses.getConstructor().newInstance();
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

 

}
