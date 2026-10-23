package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

import com.dirkfw.annotation.JsonResponse;
import com.dirkfw.annotation.SessionRequired;
import com.dirkfw.config.ConfigLoader;
import com.dirkfw.core.FrontServletParam;
import com.dirkfw.exception.SessionRequiredException;
import com.dirkfw.exception.UrlNotSupportedException;
import com.dirkfw.mapping.ModelAndView;
import com.dirkfw.mapping.UrlControllerMap;
import com.dirkfw.mapping.UrlHTTPMethod;
import com.dirkfw.mapping.UrlKey;
import com.dirkfw.session.DfwSession;
import com.dirkfw.servlet.listener.FrontServletContextListener;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class FrontServletController extends HttpServlet {

    String prefixOfView;
    String suffixOfView;
    FrontServletParam frontServletParam;
    String uploadDir;
    String authSessionName;

    @SuppressWarnings("unchecked")
    private Map<String, DfwSession> sessions;

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        frontServletParam = (FrontServletParam) getServletContext()
                .getAttribute(FrontServletContextListener.URL_PROCESSOR_ATTR);

        prefixOfView = (String) getServletContext()
                .getAttribute(FrontServletContextListener.VIEW_PREFIX);
        suffixOfView = (String) getServletContext()
                .getAttribute(FrontServletContextListener.VIEW_SUFFIX);

        uploadDir = (String) getServletContext()
                .getAttribute(FrontServletContextListener.UPLOAD_DIR_ATTR);

        sessions = (Map<String, DfwSession>) getServletContext()
                .getAttribute(FrontServletContextListener.SESSIONS_ATTR);

        ConfigLoader config = (ConfigLoader) getServletContext()
                .getAttribute(FrontServletContextListener.CONFIG_LOADER_ATTR);

        if (config != null) {
            String name = config.get(ConfigLoader.AUTH_KEY);
            if (name != null && !name.trim().isEmpty()) {
                authSessionName = name.trim();
            }
        }

        if (frontServletParam == null) {
            throw new ServletException(
                    "[dirkfw] FrontServletParam introuvable — "
                    + "FrontServletContextListener n'a pas démarré.");
        }
        if (sessions == null) {
            throw new ServletException(
                    "[dirkfw] Map des sessions introuvable dans le ServletContext.");
        }
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public static String getRequestedUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return uri.substring(context.length());
    }

    private void executeRequest(HttpServletRequest request, HttpServletResponse response)
            throws UrlNotSupportedException, ReflectiveOperationException, ServletException, IOException {

        String urlString = getRequestedUrl(request);
        UrlHTTPMethod method = UrlHTTPMethod.buildUrlHTTPMethod(request.getMethod());
        UrlKey urlKey = new UrlKey(urlString, method);

        UrlControllerMap map = frontServletParam.getUrlMapps().get(urlKey);
        if (map == null)
            throw new UrlNotSupportedException(urlKey, frontServletParam.getUrlMapps());

        Object controller = map.getControllerInstance(request);
        Method controllerMethod = map.getReflectMethod();

        checkSessionRequired(controllerMethod, request);

        Object[] args = FrontServletExecuterHelper.resolveArguments(
                this, controllerMethod, request, response);
        Object returnValueObject = controllerMethod.invoke(controller, args);

        if (returnValueObject == null)
            return;

        if (controllerMethod.isAnnotationPresent(JsonResponse.class)) {
            FrontServletExecuterHelper.writeJson(
                    controllerMethod, returnValueObject, request, response);
        } else if (returnValueObject instanceof ModelAndView) {
            ModelAndView mav = (ModelAndView) returnValueObject;
            FrontServletExecuterHelper.forwardToView(this, mav, request, response);
        }
    }

    private void checkSessionRequired(Method method, HttpServletRequest request) {
        SessionRequired sr = method.getAnnotation(SessionRequired.class);
        if (sr == null) return;

        DfwSession session = (DfwSession) request.getAttribute(DfwSession.CURRENT_ATTR);
        if (session == null) {
            throw SessionRequiredException.noSession(method.getName());
        }

        String name = sr.sessionName();

        if (name == null || name.trim().isEmpty()) {
            name = authSessionName;
        }

        if (name == null || name.trim().isEmpty()) {
            return;
        }

        Object value = session.getAttribute(name);
        if (value == null) {
            throw SessionRequiredException.missingAttribute(name);
        }

        Class<?> expected = sr.type();
        if (expected != Object.class && !expected.isInstance(value)) {
            throw SessionRequiredException.wrongType(name, expected, value.getClass());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setCharacterEncoding("UTF-8");

        HttpSession http = request.getSession(true);

        DfwSession dfw = sessions.get(http.getId());
        if (dfw == null) {
            dfw = new DfwSession(http);
            sessions.put(http.getId(), dfw);
        }

        dfw.refresh();
        request.setAttribute(DfwSession.CURRENT_ATTR, dfw);

        try {
            executeRequest(request, response);
        } catch (SessionRequiredException e) {
            if (response.isCommitted()) {
                log("[dirkfw] réponse déjà committée", e);
                return;
            }
            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            try (PrintWriter out = response.getWriter()) {
                out.println("<p>Accès non autorisé</p>");
                out.println("<pre>");
                out.println(e.getMessage());
                out.println("</pre>");
            }
        } catch (Exception e) {
            if (response.isCommitted()) {
                log("[dirkfw] réponse déjà committée, impossible de renvoyer l'erreur", e);
                return;
            }
            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            try (PrintWriter out = response.getWriter()) {
                printError(out, e);
            }
        } finally {
            try {
                dfw.flush();
            } catch (Exception ignored) {
            }
        }
    }

    private void printError(PrintWriter out, Exception e) {
        out.println("<pre>");
        e.printStackTrace(out);
        out.println("</pre>");
    }
}