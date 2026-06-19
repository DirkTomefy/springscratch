package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import com.dirkfw.util.ScanUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.dirkfw.err.UrlNotSupportedException;
import com.dirkfw.mapping.ControllerHandler;

public class FrontServletController extends HttpServlet {
    ControllerHandler ctrlHandler;
    String controllerPackageName;

    public void init() throws ServletException {
        controllerPackageName = getInitParameter("CONTROLLER_PACKAGE");
        ctrlHandler = ScanUtil.getControllerHandler(controllerPackageName);
    }

    public void verifyIfIsValidUrl(HttpServletRequest request, PrintWriter out) throws UrlNotSupportedException {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        String url = uri.substring(context.length());
        this.ctrlHandler.verifyvalidUrl(url);
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        try {
            verifyIfIsValidUrl(request, out);
            out.println("<html><body>");
            out.println("<h1>Bonjour depuis votre framework préférée !</h1>");
            out.println("<p>Vous venez de : " + request.getRequestURL().toString() + "</p>");

            out.println("<h2>Liste des Controllers : </h2>");
            for (Class<?> controller : ctrlHandler.getControllerClasses()) {
                out.println("<p>" + controller.toString() + "</p>");
            }

            out.println("<h2>Liste des Url : </h2>");
            ctrlHandler.getUrlMapps().forEach((cle, valeur) -> {
                out.println(cle + " : " + valeur.toString());
            });
            out.println("</body></html>");
        } catch (UrlNotSupportedException e) {
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
