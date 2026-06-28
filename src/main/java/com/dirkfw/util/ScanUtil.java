package com.dirkfw.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.dirkfw.annotation.Controller;
import com.dirkfw.mapping.UrlProcessor;
import com.dirkfw.util.interfaces.AnnotatedClassesProcessor;

public class ScanUtil {

    public static void handleAnnotatedClasses(Class<? extends Annotation> annotationClass,
            String packageName,
            AnnotatedClassesProcessor processor) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName == null ? "" : packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (!"file".equals(resource.getProtocol())) {
                continue;
            }
            File directory = new File(resource.toURI());
            scanDirectory(directory,
                    packageName == null ? "" : packageName,
                    classLoader,
                    annotationClass,
                    processor);
        }
    }

    private static void scanDirectory(File directory, String packageName,
            ClassLoader classLoader,
            Class<? extends Annotation> annotationClass,
            AnnotatedClassesProcessor processor) throws Exception {
        if (!directory.exists()) {
            return;
        }

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file,
                        joinPackage(packageName, file.getName()),
                        classLoader,
                        annotationClass,
                        processor);
            } else if (file.getName().endsWith(".class")) {
                String simpleName = file.getName().substring(0, file.getName().length() - ".class".length());
                String className = joinPackage(packageName, simpleName);
                Class<?> clazz = classLoader.loadClass(className);
                if (clazz.isAnnotationPresent(annotationClass)) {
                    processor.process(clazz);
                }
            }
        }
    }


    private static String joinPackage(String packageName, String name) {
        if (packageName == null || packageName.isBlank()) {
            return name;
        }
        return packageName + "." + name;
    }

    public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        List<Method> methods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                methods.add(method);
            }
        }
        return methods;
    }

    public static void getControllerHandler(String packageName, UrlProcessor urlProcessor) throws Exception {
        handleAnnotatedClasses(Controller.class, packageName, urlProcessor);
    }
}