package com.dirkfw.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.dirkfw.annotation.Controller;

public class ScanUtil {
    public static Set<Class<?>> getAllPackageFromClassPath(){
          Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forClassLoader())
            .setScanners(new SubTypesScanner(false))
        );

        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

        return classes;
    }  
    public static List<Class<?>> getAllControllerFromClassPath(){
        List<Class<?>> retournClasses = new ArrayList<>();
        Set<Class<?>> classes = getAllPackageFromClassPath();

        for (Class<?> c : classes) {
            if(c.isAnnotationPresent(Controller.class))
                retournClasses.add(c);
        }
        return retournClasses;
    }

}
