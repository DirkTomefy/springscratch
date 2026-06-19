package com.dirkfw.mapping;

import java.lang.reflect.Method;


public class UrlControllerMap {
    Method method;
    Class<?> controllerClasses;
    //autre information comme base_url ou autre type (get ou post)

    public UrlControllerMap(Method method, Class<?> controllerClasses) {
        this.method = method;
        this.controllerClasses = controllerClasses;
    }

    

    public void initializeAllField(Method method, Class<?> controllerClasses) {
        this.method = method;
        this.controllerClasses = controllerClasses;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?> getControllerClasses() {
        return controllerClasses;
    }

    public void setControllerClasses(Class<?> controllerClasses) {
        this.controllerClasses = controllerClasses;
    }

}
