package com.dirkfw.classes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dirkfw.annotation.Controller;
import com.dirkfw.annotation.UrlMapping;
import com.dirkfw.classes.key.UrlKey;
import com.dirkfw.classes.mapping.UrlControllerMap;
import com.dirkfw.err.UrlAlreadyDefinedException;
import com.dirkfw.err.UrlNotSupportedException;
import com.dirkfw.util.interfaces.AnnotatedClassesProcessor;

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

    public void executeRequest(UrlKey url)
            throws UrlNotSupportedException, ReflectiveOperationException {
        if (!this.getUrlMapps().containsKey(url)) {
            throw new UrlNotSupportedException(url, urlMapps);
        }
        UrlControllerMap map = this.getUrlMapps().get(url);
        map.getReflectMethod().invoke(map.getPrototypeSeed());
    }

    public List<Class<?>> getControllerClasses() {
        return controllerClasses;
    }

    public HashMap<UrlKey, UrlControllerMap> getUrlMapps() {
        return urlMapps;
    }
}