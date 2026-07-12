package com.dirkfw.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.dirkfw.servlet.listener.FrontServletContextListener;

import jakarta.servlet.ServletContext;

public class SpringBeanProvider extends BeanProvider {

    private final Object applicationContext;

    public SpringBeanProvider(ServletContext servletContext) {

        super(servletContext);

        try {

            String configClassName =
                    servletContext.getInitParameter(
                            FrontServletContextListener.SPRING_CONFIGURATION);

            if (configClassName == null || configClassName.isBlank()) {
                throw new RuntimeException(
                        "Le paramètre '"
                                + FrontServletContextListener.SPRING_CONFIGURATION
                                + "' est obligatoire.");
            }

            Class<?> configurationClass = Class.forName(configClassName);

          
            Class<?> annotationContextClass = Class.forName(
                    "org.springframework.context.annotation.AnnotationConfigApplicationContext");

            Constructor<?> constructor =
                    annotationContextClass.getConstructor(Class[].class);

            applicationContext = constructor.newInstance(
                    (Object) new Class<?>[]{configurationClass});

        } catch (Exception e) {
            throw new RuntimeException(
                    "Impossible d'initialiser le contexte Spring.", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {

        try {

            Method method = applicationContext
                    .getClass()
                    .getMethod("getBean", Class.class);

            return (T) method.invoke(applicationContext, type);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Impossible de récupérer le bean Spring : "
                            + type.getName(),
                    e);
        }
    }

    @Override
    public boolean containsBean(Class<?> type) {

        try {

            Method method = applicationContext
                    .getClass()
                    .getMethod("getBeanNamesForType", Class.class);

            String[] names =
                    (String[]) method.invoke(applicationContext, type);

            return names.length > 0;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void processOnScan(Class<?> type) {
        //RIEN FAIRE : (spring gère déja ici)
    }

}