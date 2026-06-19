package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.dirkfw.util.ScanUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.dirkfw.annotation.Controller;

public class FrontServletController extends HttpServlet {
    List<String> controllerClassesInString = new ArrayList<>();
    String controllerPackageName;
    public void init() throws ServletException {
        controllerPackageName=getInitParameter("CONTROLLER_PACKAGE");
        for (Class<?> c : ScanUtil.findAllClassesFromPackageAndAnnotation(controllerPackageName,Controller.class)) {
            controllerClassesInString.add(c.toString());
        }
    }

    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>Bonjour depuis votre framework préférée !</h1>");
        out.println("<p>Vous venez de : " + request.getRequestURL().toString() + "</p>");
        for (String cString : controllerClassesInString) {
            out.println("<p>" + cString + "</p>");
        }
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
