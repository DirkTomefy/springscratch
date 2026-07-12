package com.dirkfw.container;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.dirkfw.annotation.scope.PrototypeScope;

import jakarta.servlet.ServletContext;

public class DfwBeanProvider extends BeanProvider {

    public DfwBeanProvider(ServletContext context) {
        super(context);
    }



    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<String, Object> namedBeans = new HashMap<>();


    public void registerBean(Class<?> clazz) {
        getBean(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {

        if (type.isAnnotationPresent(PrototypeScope.class)) {
            return (T) createInstance(type);
        }

        return (T) singletons.computeIfAbsent(type, clazz -> {
            Object instance = createInstance(clazz);
            namedBeans.put(clazz.getSimpleName(), instance);
            return instance;
        });
    }

    private Object createInstance(Class<?> type) {

        try {

            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);

            return constructor.newInstance();

        } catch (Exception e) {
            throw new RuntimeException(
                "Impossible d'instancier " + type.getName(), e);
        }
    }

   

    @Override
    public boolean containsBean(Class<?> type) {

        if (type.isAnnotationPresent(PrototypeScope.class)) {
            return true;
        }

        return singletons.containsKey(type);
    }

    @Override
    public void processOnScan(Class<?> type) {
        registerBean(type);
    }

}