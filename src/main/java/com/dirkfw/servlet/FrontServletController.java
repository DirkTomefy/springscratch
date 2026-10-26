package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.dirkfw.annotation.JsonResponse;
import com.dirkfw.annotation.SessionRequired;
import com.dirkfw.config.ConfigLoader;
import com.dirkfw.core.FrontServletParam;
import com.dirkfw.exception.DfwException;
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
    String authRoleName;

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
            String role = config.get(ConfigLoader.ROLE_KEY);
            if (role != null && !role.trim().isEmpty()) {
                authRoleName = role.trim();
            }
        }

        log("[dirkfw] ConfigLoader=" + (config == null ? "ABSENT (attribut "
                + FrontServletContextListener.CONFIG_LOADER_ATTR + " non défini)" : "OK")
                + ", authSessionName=" + authSessionName
                + ", authRoleName=" + authRoleName
                + ", uploadDir=" + uploadDir);

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
            throws Exception {

        UrlKey urlKey = buildUrlKey(request);

        UrlControllerMap map = frontServletParam.getUrlMapps().get(urlKey);
        if (map == null)
            throw new UrlNotSupportedException(urlKey, frontServletParam.getUrlMapps());

        Object controller = map.getControllerInstance(request);
        Method controllerMethod = map.getReflectMethod();

        checkSessionRequired(controllerMethod, request);

        Object[] args = FrontServletExecuterHelper.resolveArguments(
                this, controllerMethod, request, response);

        Object returnValueObject = invokeController(controller, controllerMethod, args);

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

   
    private Object invokeController(Object controller, Method method, Object[] args)
            throws Exception {
        try {
            return method.invoke(controller, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) throw (Exception) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw e;
        }
    }

    private void checkSessionRequired(Method method, HttpServletRequest request) {
        SessionRequired sr = method.getAnnotation(SessionRequired.class);
        if (sr == null) return;

        DfwSession session = (DfwSession) request.getAttribute(DfwSession.CURRENT_ATTR);
        if (session == null) {
            throw SessionRequiredException.noSession(method.getName());
        }

        if (authSessionName == null) {
            throw new DfwException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "@SessionRequired sur la méthode '" + method.getName()
                    + "' mais la clé " + ConfigLoader.AUTH_KEY + " n'est pas configurée "
                    + "dans " + ConfigLoader.DIRKFW_PROPERTIES_FILE + ".");
        }

        Object value = session.getAttribute(authSessionName);
        if (value == null) {
            throw SessionRequiredException.missingAttribute(authSessionName);
        }

        Class<?> expected = sr.type();
        if (expected != Object.class && !expected.isInstance(value)) {
            throw SessionRequiredException.wrongType(authSessionName, expected, value.getClass());
        }

        if (sr.value().length > 0) {
            checkRole(method, session, sr.value());
        }
    }

    private void checkRole(Method method, DfwSession session, String[] allowedRoles) {
        if (authRoleName == null) {
            throw new DfwException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "@SessionRequired(value = ...) sur la méthode '" + method.getName()
                    + "' mais la clé " + ConfigLoader.ROLE_KEY + " n'est pas configurée "
                    + "dans " + ConfigLoader.DIRKFW_PROPERTIES_FILE + ".");
        }

        Object role = session.getAttribute(authRoleName);
        if (role == null) {
            throw SessionRequiredException.missingAttribute(authRoleName);
        }

        for (String allowed : allowedRoles) {
            if (allowed.equals(String.valueOf(role))) return;
        }
        throw new SessionRequiredException("Rôle '" + role + "' non autorisé pour "
                + method.getName());
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
        } catch (Exception e) {
            handleException(e, request, response);
        } finally {
            try {
                dfw.flush();
            } catch (Exception e) {
                log("[dirkfw] échec du flush de la session " + http.getId(), e);
            }
        }
    }

   
    private void handleException(Exception e, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        int status = resolveStatus(e);

        if (status >= 500) {
            log("[dirkfw] erreur " + status + " sur " + request.getMethod()
                    + " " + getRequestedUrl(request), e);
        } else {
            log("[dirkfw] " + status + " " + request.getMethod() + " "
                    + getRequestedUrl(request) + " : " + e.getMessage());
        }

        if (response.isCommitted()) {
            log("[dirkfw] réponse déjà committée, impossible de renvoyer l'erreur");
            return;
        }

        response.resetBuffer();
        response.setStatus(status);

        if (isJsonRequest(request)) {
            writeJsonError(response, status, e);
        } else {
            writeHtmlError(response, status, e);
        }
    }

    private int resolveStatus(Exception e) {
        if (e instanceof DfwException) {
            return ((DfwException) e).getHttpStatus();
        }
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        UrlControllerMap map = frontServletParam.getUrlMapps().get(buildUrlKey(request));
        return map != null && map.getReflectMethod().isAnnotationPresent(JsonResponse.class);
    }

    private UrlKey buildUrlKey(HttpServletRequest request) {
        UrlHTTPMethod method = UrlHTTPMethod.buildUrlHTTPMethod(request.getMethod());
        return new UrlKey(getRequestedUrl(request), method);
    }

    private String publicMessage(int status, Exception e) {
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }

    private void writeJsonError(HttpServletResponse response, int status, Exception e)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            StringBuilder json = new StringBuilder();
            json.append("{\"status\":").append(status)
                .append(",\"error\":\"").append(escapeJson(publicMessage(status, e))).append("\"");

            if (e instanceof UrlNotSupportedException) {
                json.append(",\"supportedUrls\":[");
                boolean first = true;
                for (UrlKey key : ((UrlNotSupportedException) e).getSupportedUrl().keySet()) {
                    if (!first) json.append(",");
                    json.append("\"").append(escapeJson(String.valueOf(key))).append("\"");
                    first = false;
                }
                json.append("]");
            }

            json.append(",\"exception\":\"").append(escapeJson(e.getClass().getName())).append("\"");
            json.append(",\"trace\":\"").append(escapeJson(stackTrace(e))).append("\"");

            json.append("}");
            out.print(json);
        }
    }

    private void writeHtmlError(HttpServletResponse response, int status, Exception e)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<h3>Erreur " + status + "</h3>");
            out.println("<p>" + escapeHtml(publicMessage(status, e)) + "</p>");

            if (e instanceof UrlNotSupportedException) {
                out.println("<pre>");
                out.println(escapeHtml(e.toString()));
                out.println("</pre>");
            }

            out.println("<pre>");
            out.println(escapeHtml(stackTrace(e)));
            out.println("</pre>");
        }
    }

    private static String stackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }
}