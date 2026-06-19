package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import com.dirkfw.util.ScanUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.dirkfw.mapping.ControllerHandler;

public class FrontServletController extends HttpServlet {
    ControllerHandler ctrlHandler;
    String controllerPackageName;

    public void init() throws ServletException {
        controllerPackageName = getInitParameter("CONTROLLER_PACKAGE");
        ctrlHandler = ScanUtil.getControllerHandler(controllerPackageName);
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>Bonjour depuis votre framework préférée !</h1>");
        out.println("<p>Vous venez de : " + request.getRequestURL().toString() + "</p>");
        for (Class<?> controller : ctrlHandler.getControllerClasses()) {
            out.println("<p>" + controller.toString() + "</p>");
        }
        ctrlHandler.getUrlMapps().forEach((cle, valeur) -> {
            out.println(cle + " : " + valeur);
        });
        out.println("</body></html>");
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
