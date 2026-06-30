package com.dirkfw.servlet.listener;


import com.dirkfw.mapping.UrlProcessor;
import com.dirkfw.util.ScanUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class FrontServletContextListener implements ServletContextListener {

    public static final String URL_PROCESSOR_ATTR = "urlProcessor";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        String controllerPackage = context.getInitParameter("CONTROLLER_PACKAGE");
        if (controllerPackage == null) {
            controllerPackage = ""; 
        }

        UrlProcessor urlProcessor = new UrlProcessor();
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
