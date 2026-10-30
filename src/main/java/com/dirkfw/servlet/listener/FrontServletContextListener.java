package com.dirkfw.servlet.listener;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dirkfw.core.FrontServletParam;
import com.dirkfw.session.DfwSession;
import com.dirkfw.util.ScanUtil;
import com.dirkfw.config.ConfigLoader;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@WebListener
public class FrontServletContextListener
        implements ServletContextListener, HttpSessionListener {

    public static final String URL_PROCESSOR_ATTR = "urlProcessor";
    public static final String VIEW_PREFIX         = "VIEW_PREFIX";
    public static final String VIEW_SUFFIX         = "VIEW_SUFFIX";
    public static final String CONTROLLER_PACKAGE  = "CONTROLLER_PACKAGE";
    public static final String SPRING_CONFIGURATION = "SPRING_CONFIGURATION";

    public static final String CONFIG_LOADER_ATTR  = "dirkfwConfigLoader";
    public static final String UPLOAD_DIR_ATTR     = "dirkfwUploadDir";
    public static final String SESSIONS_ATTR       = "dirkfwSessions";
    public static final String CONTROLLERS_ATTR    = "dirkfwControllers";

    private Object springContext;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        String controllerPackage = getControllerPackage(context);
        String viewPrefix = context.getInitParameter(VIEW_PREFIX);
        String viewSuffix = context.getInitParameter(VIEW_SUFFIX);

        ConfigLoader config = loadConfig(context);

        Object springContext = createSpringContext(context);
        this.springContext = springContext;

        FrontServletParam urlProcessor = createUrlProcessor(
                controllerPackage,
                springContext
        );

        Map<String, DfwSession> sessions = new ConcurrentHashMap<>();
        context.setAttribute(SESSIONS_ATTR, sessions);

        Map<Class<?>, Object> controllers = new HashMap<>();
        for (Class<?> controllerClass : urlProcessor.getControllerClasses()) {
            controllers.put(controllerClass, instantiate(controllerClass));
        }
        context.setAttribute(CONTROLLERS_ATTR, controllers);

        registerContextAttributes(
                context,
                urlProcessor,
                viewPrefix,
                viewSuffix,
                config
        );
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (springContext instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                sce.getServletContext().log("Fermeture contexte Spring échouée", e);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void sessionDestroyed(HttpSessionEvent se) {
        Map<String, DfwSession> sessions = (Map<String, DfwSession>)
                se.getSession().getServletContext().getAttribute(SESSIONS_ATTR);
        if (sessions != null) {
            sessions.remove(se.getSession().getId());
        }
    }

    private ConfigLoader loadConfig(ServletContext context) {
        try {
            ConfigLoader loader = new ConfigLoader(
                    ConfigLoader.DIRKFW_PROPERTIES_FILE);

            String uploadDir = loader.get(ConfigLoader.UPLOAD_DIR_KEY);
            if (uploadDir == null || uploadDir.trim().isEmpty()) {
                uploadDir = defaultUploadDir(context);
                context.log("[dirkfw] '" + ConfigLoader.UPLOAD_DIR_KEY
                        + "' non défini, fallback : " + uploadDir);
            } else {
                uploadDir = uploadDir.trim();
            }
            context.setAttribute(UPLOAD_DIR_ATTR, uploadDir);

            return loader;

        } catch (RuntimeException e) {
            context.log("[dirkfw] " + ConfigLoader.DIRKFW_PROPERTIES_FILE
                    + " introuvable, utilisation du fallback", e);

            context.setAttribute(UPLOAD_DIR_ATTR, defaultUploadDir(context));
            return null;
        }
    }

    private String defaultUploadDir(ServletContext context) {
        String base = System.getProperty("catalina.base",
                System.getProperty("java.io.tmpdir"));
        return base + "/dirkfw-uploads";
    }

    private String getControllerPackage(ServletContext context) {
        String value = context.getInitParameter(CONTROLLER_PACKAGE);
        return value == null ? "" : value;
    }

    private Object instantiate(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object instance = constructor.newInstance();
            System.out.println(type.getName() + " est instancié");
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Impossible d'instancier " + type.getName(), e);
        }
    }

    private Object createSpringContext(ServletContext context) {

        String configClassName = context.getInitParameter(SPRING_CONFIGURATION);

        if (configClassName == null || configClassName.trim().isEmpty()) {
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
            Object springContext) {

        FrontServletParam processor = new FrontServletParam(springContext);

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
            String viewPrefix,
            String viewSuffix,
            ConfigLoader config) {

        context.setAttribute(URL_PROCESSOR_ATTR, processor);
        context.setAttribute(VIEW_PREFIX, viewPrefix);
        context.setAttribute(VIEW_SUFFIX, viewSuffix);

        if (config != null) {
            context.setAttribute(CONFIG_LOADER_ATTR, config);
        }
    }
}