package com.dirkfw.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dirkfw.annotation.Controller;
import com.dirkfw.annotation.UrlMapping;
import com.dirkfw.container.ControllerProvider;
import com.dirkfw.mapping.UrlKey;
import com.dirkfw.mapping.UrlControllerMap;
import com.dirkfw.exception.UrlAlreadyDefinedException;
import com.dirkfw.util.AnnotatedClassesProcessor;

public class FrontServletParam implements AnnotatedClassesProcessor {

    private final ControllerProvider beanProvider ;
    private final List<Class<?>> controllerClasses = new ArrayList<>();
    private final HashMap<UrlKey, UrlControllerMap> urlMapps = new HashMap<>();
    private Object externalContext;

    public FrontServletParam(ControllerProvider beanProvider,Object externalContext){
        this.beanProvider=beanProvider;
        this.externalContext=externalContext;
    }
    @Override
    public void processAnnotatedClass(Class<?> clazz) throws Exception {
        if (!clazz.isAnnotationPresent(Controller.class)) {
            return;
        }

        controllerClasses.add(clazz);

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(UrlMapping.class)) {
                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                UrlKey key = new UrlKey(urlMapping.value(), urlMapping.httpMethod());

                if (urlMapps.containsKey(key)) {
                    throw new UrlAlreadyDefinedException(key, urlMapps.get(key));
                } else {
                    urlMapps.put(key, new UrlControllerMap(method, clazz,beanProvider));
                    beanProvider.processOnScan(clazz);
                }
            }
        }
    }

    public List<Class<?>> getControllerClasses() {
        return controllerClasses;
    }

    public HashMap<UrlKey, UrlControllerMap> getUrlMapps() {
        return urlMapps;
    }

        public ControllerProvider getBeanProvider() {
        return beanProvider;
    }

    
    public Object getExternalContext() {
        return externalContext;
    }

    public void setExternalContext(Object externalContext) {
        this.externalContext = externalContext;
    }

}