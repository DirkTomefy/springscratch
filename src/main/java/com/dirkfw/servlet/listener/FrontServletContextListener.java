package com.dirkfw.servlet.listener;


import com.dirkfw.classes.FrontServletParam;
import com.dirkfw.util.ScanUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class FrontServletContextListener implements ServletContextListener {

    public static final String URL_PROCESSOR_ATTR = "urlProcessor";
    public static final String VIEW_PREFIX="VIEW_PREFIX";
    public static final String VIEW_SUFFIX="VIEW_SUFFIX";

    public static final String CONTROLLER_PACKAGE="CONTROLLER_PACKAGE";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        String controllerPackage = context.getInitParameter(CONTROLLER_PACKAGE);
        String viewPrefixValue = context.getInitParameter(VIEW_PREFIX);
        String viewSuffixValue = context.getInitParameter(VIEW_SUFFIX);

        if (controllerPackage == null) {
            controllerPackage = ""; 
        }

        FrontServletParam urlProcessor = new FrontServletParam();
        try {
            ScanUtil.fillUrlProcessor(controllerPackage, urlProcessor);
        } catch (Exception e) {
            System.out.println("CAUSE DE L'ERREUR : "+e.getMessage());
            throw new RuntimeException("Erreur lors de l'initialisation du UrlProcessor", e);
        }

        context.setAttribute(URL_PROCESSOR_ATTR, urlProcessor);
        context.setAttribute(VIEW_PREFIX, viewPrefixValue);
        context.setAttribute(VIEW_SUFFIX, viewSuffixValue);

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
    }
}
