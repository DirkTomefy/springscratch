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
    public static final String CONTROLLER_PACKAGE="CONTROLLER_PACKAGE";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        String controllerPackage = context.getInitParameter(CONTROLLER_PACKAGE);
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
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
    }
}
