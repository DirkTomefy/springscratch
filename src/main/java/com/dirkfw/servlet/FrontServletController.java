package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import com.dirkfw.util.ScanUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.dirkfw.err.UrlNotSupportedException;
import com.dirkfw.mapping.UrlHTTPMethod;
import com.dirkfw.mapping.UrlKey;
import com.dirkfw.mapping.UrlProcessor;

public class FrontServletController extends HttpServlet {
    UrlProcessor urlProcessor;
    String controllerPackageName;

    public void init() throws ServletException {
        controllerPackageName = getInitParameter("CONTROLLER_PACKAGE");
        urlProcessor = new UrlProcessor();
        try {
            ScanUtil.fillUrlProcessor(controllerPackageName, urlProcessor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeRequest(HttpServletRequest request, PrintWriter out) throws UrlNotSupportedException, IllegalAccessException, InvocationTargetException {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        String url = uri.substring(context.length());
        String method = request.getMethod();
        this.urlProcessor.executeRequest(new UrlKey(url, UrlHTTPMethod.buildUrlHTTPMethod(method)));
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        try {
            executeRequest(request, out);
            out.println("<html><body>");
            out.println("<h1>Bonjour depuis votre framework préférée !</h1>");
            out.println("<p>Vous venez de : " + request.getRequestURL().toString() + "</p>");

            out.println("<h2>Liste des Controllers : </h2>");
            for (Class<?> controller : urlProcessor.getControllerClasses()) {
                out.println("<p>" + controller.toString() + "</p>");
            }

            out.println("<h2>Liste des Url : </h2>");
            urlProcessor.getUrlMapps().forEach((cle, valeur) -> {
                out.print("<p>");
                out.println(cle + " : " + valeur.toString());
                out.print("</p>");
            });
            out.println("</body></html>");
        } catch (UrlNotSupportedException | IllegalAccessException | InvocationTargetException e) {
            out.println("<p>" + e.toString() + "</p>");
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
