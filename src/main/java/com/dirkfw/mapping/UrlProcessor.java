package com.dirkfw.mapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dirkfw.annotation.Controller;
import com.dirkfw.annotation.UrlMapping;
import com.dirkfw.err.UrlNotSupportedException;
import com.dirkfw.util.interfaces.AnnotatedClassesProcessor;

public class UrlProcessor implements AnnotatedClassesProcessor {

    private final List<Class<?>> controllerClasses = new ArrayList<>();
    private final HashMap<String, UrlControllerMap> urlMapps = new HashMap<>();

    @Override
    public void process(Class<?> clazz) throws Exception {
       
        if (clazz.isAnnotationPresent(Controller.class)) {
            controllerClasses.add(clazz);
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                    urlMapps.put(urlMapping.value(), new UrlControllerMap(method, clazz));
                }
            }
        }
    }

    public void verifyvalidUrl(String url) throws UrlNotSupportedException {
        if(!this.getUrlMapps().containsKey(url)) throw new UrlNotSupportedException(url, urlMapps);
    }
  

    public List<Class<?>> getControllerClasses() {
        return controllerClasses;
    }

    public HashMap<String, UrlControllerMap> getUrlMapps() {
        return urlMapps;
    }
}