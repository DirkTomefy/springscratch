package com.dirkfw.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dirkfw.annotation.Controller;
import com.dirkfw.annotation.UrlMapping;
import com.dirkfw.mapping.UrlKey;
import com.dirkfw.mapping.UrlControllerMap;
import com.dirkfw.exception.UrlAlreadyDefinedException;
import com.dirkfw.util.AnnotatedClassesProcessor;

public class FrontServletParam implements AnnotatedClassesProcessor {

    private final List<Class<?>> controllerClasses = new ArrayList<>();
    private final HashMap<UrlKey, UrlControllerMap> urlMapps = new HashMap<>();

    @Override
    public void processAnnotatedClass(Class<?> clazz) throws ReflectiveOperationException,UrlAlreadyDefinedException {

        if (clazz.isAnnotationPresent(Controller.class)) {
            controllerClasses.add(clazz);
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                    UrlKey key = new UrlKey(urlMapping.value(), urlMapping.httpMethod());
                    if (urlMapps.containsKey(key)) {
                        throw new UrlAlreadyDefinedException(key, urlMapps.get(key));
                    } else {
                        urlMapps.put(key,
                                new UrlControllerMap(method, clazz));
                    }

                }
            }
        } else {
            return;
        }

    }

    public List<Class<?>> getControllerClasses() {
        return controllerClasses;
    }

    public HashMap<UrlKey, UrlControllerMap> getUrlMapps() {
        return urlMapps;
    }
}