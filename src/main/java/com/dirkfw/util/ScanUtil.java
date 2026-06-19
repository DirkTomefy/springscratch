package com.dirkfw.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

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

}
