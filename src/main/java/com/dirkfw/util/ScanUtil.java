package com.dirkfw.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.dirkfw.annotation.Controller;
import com.dirkfw.annotation.UrlMapping;
import com.dirkfw.mapping.ControllerHandler;
import com.dirkfw.mapping.UrlControllerMap;

public class ScanUtil {

    public static Reflections getReflectionObject(String packageName) {
        ConfigurationBuilder config = new ConfigurationBuilder();

        if (packageName == null || packageName.trim().isEmpty()) {
            config.setUrls(ClasspathHelper.forClassLoader());
        } else {
            config.forPackages(packageName);
        }

        config.setScanners(Scanners.SubTypes, Scanners.TypesAnnotated);

        return new Reflections(config);
    }

    public static List<Class<?>> findAllClassesFromPackageAndAnnotation(String packageName,
            Class<? extends Annotation> annotationToFind) {
        Reflections reflections = getReflectionObject(packageName);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotationToFind);
        return new ArrayList<>(annotatedClasses);
    }

    public static void addUrlControllerMapFromClass(HashMap<String, UrlControllerMap> maps, Class<?> ctrlClass) {
        for (Method method : ctrlClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(UrlMapping.class)) {
                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                maps.put(urlMapping.value(), new UrlControllerMap(method, ctrlClass));
            }
        }
    }

    public static ControllerHandler getControllerHandler(String packageName) {
        List<Class<?>> controllerClasses = findAllClassesFromPackageAndAnnotation(packageName, Controller.class);
        HashMap<String, UrlControllerMap> urlMapps = new HashMap<>();
        for (Class<?> ctrlClass : controllerClasses) {
            addUrlControllerMapFromClass(urlMapps, ctrlClass);
        }
        return new ControllerHandler(controllerClasses, urlMapps);
    }

}
