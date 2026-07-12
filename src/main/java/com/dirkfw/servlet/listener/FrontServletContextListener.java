package com.dirkfw.servlet.listener;

import java.lang.reflect.Constructor;

import com.dirkfw.container.BeanProvider;
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

        String controllerPackage = context.getInitParameter(CONTROLLER_PACKAGE);
        String viewPrefixValue = context.getInitParameter(VIEW_PREFIX);
        String viewSuffixValue = context.getInitParameter(VIEW_SUFFIX);

        if (controllerPackage == null) {
            controllerPackage = "";
        }

        BeanProvider beanProvider;

        try {

            String providerClassName = context.getInitParameter(BEAN_PROVIDER_CLASS);

            if (providerClassName == null || providerClassName.isBlank()) {

                beanProvider = new DfwBeanProvider(context);

            } else {

                Class<?> providerClass = Class.forName(providerClassName);

                if (!BeanProvider.class.isAssignableFrom(providerClass)) {
                    throw new RuntimeException(
                            providerClassName + " doit hériter de "
                                    + BeanProvider.class.getName());
                }

                Constructor<?> constructor =
                        providerClass.getConstructor(ServletContext.class);

                beanProvider =
                        (BeanProvider) constructor.newInstance(context);

            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Impossible d'initialiser le BeanProvider.", e);
        }

        FrontServletParam urlProcessor = new FrontServletParam(beanProvider);

        try {
            ScanUtil.fillUrlProcessor(controllerPackage, urlProcessor);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Erreur lors de l'initialisation du UrlProcessor", e);
        }

        context.setAttribute(URL_PROCESSOR_ATTR, urlProcessor);
        context.setAttribute(VIEW_PREFIX, viewPrefixValue);
        context.setAttribute(VIEW_SUFFIX, viewSuffixValue);
        context.setAttribute(BEAN_PROVIDER_CLASS, beanProvider);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Rien à faire
    }
}