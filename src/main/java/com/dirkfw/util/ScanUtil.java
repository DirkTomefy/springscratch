package com.dirkfw.util;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import com.dirkfw.mapping.UrlProcessor;
import com.dirkfw.util.interfaces.AnnotatedClassesProcessor;

public class ScanUtil {

    public static void handleAnnotatedClasses(
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
                    processor);
        }
    }

    private static void scanDirectory(File directory, String packageName,
            ClassLoader classLoader,
            AnnotatedClassesProcessor processor) throws Exception {
        if (!directory.exists()) {
            return;
        }

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file,
                        joinPackage(packageName, file.getName()),
                        classLoader,
                        processor);
            } else if (file.getName().endsWith(".class")) {
                String simpleName = file.getName().substring(0, file.getName().length() - ".class".length());
                String className = joinPackage(packageName, simpleName);
                Class<?> clazz = classLoader.loadClass(className);
                processor.processAnnotatedClass(clazz);
            }
        }
    }


    private static String joinPackage(String packageName, String name) {
        if (packageName == null || packageName.isBlank()) {
            return name;
        }
        return packageName + "." + name;
    }


    public static void fillUrlProcessor(String packageName, UrlProcessor urlProcessor) throws Exception {
        handleAnnotatedClasses(packageName, urlProcessor);
    }
}