package com.dirkfw.servlet.listener;

import java.lang.reflect.Constructor;

import com.dirkfw.container.ControllerProvider;
import com.dirkfw.container.DfwBeanProvider;
import com.dirkfw.core.FrontServletParam;
import com.dirkfw.util.ScanUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class FrontServletContextListener implements ServletContextListener {

    public static final String URL_PROCESSOR_ATTR = "urlProcessor";
    public static final String VIEW_PREFIX = "VIEW_PREFIX";
    public static final String VIEW_SUFFIX = "VIEW_SUFFIX";
    public static final String CONTROLLER_PACKAGE = "CONTROLLER_PACKAGE";
    public static final String BEAN_PROVIDER_CLASS = "BEAN_PROVIDER";
    public static final String SPRING_CONFIGURATION = "SPRING_CONFIGURATION";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        String controllerPackage = getControllerPackage(context);
        String viewPrefix = context.getInitParameter(VIEW_PREFIX);
        String viewSuffix = context.getInitParameter(VIEW_SUFFIX);

        Object springContext = createSpringContext(context);
        ControllerProvider beanProvider = createBeanProvider(context,springContext);
       

        FrontServletParam urlProcessor = createUrlProcessor(
                controllerPackage,
                beanProvider,
                springContext
        );

        registerContextAttributes(
                context,
                urlProcessor,
                beanProvider,
                viewPrefix,
                viewSuffix
        );
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    private String getControllerPackage(ServletContext context) {
        String value = context.getInitParameter(CONTROLLER_PACKAGE);
        return value == null ? "" : value;
    }

    private ControllerProvider createBeanProvider(ServletContext context,Object externalContext) {
        try {
            String providerClassName = context.getInitParameter(BEAN_PROVIDER_CLASS);

            if (providerClassName == null || providerClassName.isBlank()) {
                return new DfwBeanProvider(context,externalContext);
            }

            Class<?> providerClass = Class.forName(providerClassName);

            if (!ControllerProvider.class.isAssignableFrom(providerClass)) {
                throw new RuntimeException(
                        providerClassName + " doit hériter de " + ControllerProvider.class.getName());
            }

            Constructor<?> constructor =
                    providerClass.getConstructor(ServletContext.class);

            return (ControllerProvider) constructor.newInstance(context);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Impossible d'initialiser le BeanProvider.", e);
        }
    }

    private Object createSpringContext(ServletContext context) {

        String configClassName = context.getInitParameter(SPRING_CONFIGURATION);

        if (configClassName == null || configClassName.isBlank()) {
            return null;
        }

        try {
            Class<?> annotationContext =
                    Class.forName(
                            "org.springframework.context.annotation.AnnotationConfigApplicationContext");

            Class<?> configurationClass =
                    Class.forName(configClassName);

            Constructor<?> constructor =
                    annotationContext.getConstructor(Class[].class);

            return constructor.newInstance(
                    (Object) new Class<?>[]{configurationClass});

        } catch (Exception e) {
            throw new RuntimeException(
                    "Impossible d'initialiser le contexte Spring.", e);
        }
    }

    private FrontServletParam createUrlProcessor(
            String controllerPackage,
            ControllerProvider beanProvider,
            Object springContext) {

        FrontServletParam processor = new FrontServletParam(beanProvider,springContext);

        try {
            ScanUtil.fillUrlProcessor(controllerPackage, processor);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur lors de l'initialisation du UrlProcessor", e);
        }

        return processor;
    }

    private void registerContextAttributes(
            ServletContext context,
            FrontServletParam processor,
            ControllerProvider beanProvider,
            String viewPrefix,
            String viewSuffix) {

        context.setAttribute(URL_PROCESSOR_ATTR, processor);
        context.setAttribute(VIEW_PREFIX, viewPrefix);
        context.setAttribute(VIEW_SUFFIX, viewSuffix);
        context.setAttribute(BEAN_PROVIDER_CLASS, beanProvider);
    }
}