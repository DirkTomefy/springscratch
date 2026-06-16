package com.dirkfw.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class ScanUtil {
    public List<Class<?>> getAllPackageFromClassPath(){
        List<Class<?>> listofClasses=new ArrayList<>();
          Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forClassLoader())
            .setScanners(new SubTypesScanner(false))
        );

        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

        for (Class<?> clazz : classes) {
            listofClasses.add(clazz);
        }
        return listofClasses;
    }  
}
