package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import com.dirkfw.classes.FrontServletParam;
import com.dirkfw.classes.helper.UrlHTTPMethod;
import com.dirkfw.classes.key.UrlKey;
import com.dirkfw.err.UrlNotSupportedException;
import com.dirkfw.servlet.listener.FrontServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServletController extends HttpServlet {

    private FrontServletParam urlProcessor;

    @Override
    public void init() throws ServletException {
        urlProcessor = (FrontServletParam) getServletContext()
                    .getAttribute(FrontServletContextListener.URL_PROCESSOR_ATTR);
    }

    private void executeRequest(HttpServletRequest request)
            throws UrlNotSupportedException, ReflectiveOperationException {

        String url = getRequestedUrl(request);
        UrlHTTPMethod method = UrlHTTPMethod.buildUrlHTTPMethod(request.getMethod());

        urlProcessor.executeRequest(new UrlKey(url, method));
    }

    private String getRequestedUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return uri.substring(context.length());
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

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        try {
            executeRequest(request);
            printDebugPage(request, out);
        } catch (UrlNotSupportedException e) {
            printError(out, e.toString());

        } catch (ReflectiveOperationException e) {
            printError(out, e.getMessage());
            e.printStackTrace();
        }
        out.close();
    }

    private void printDebugPage(HttpServletRequest request, PrintWriter out) {

        out.println("<html><body>");

        printHeader(request, out);
        printControllers(out);
        printMappings(out);

        out.println("</body></html>");
    }

    private void printHeader(HttpServletRequest request, PrintWriter out) {

        out.println("<h1>Bonjour depuis votre framework préféré !</h1>");
        out.println("<p>Vous venez de : " + request.getRequestURL() + "</p>");
    }

    private void printControllers(PrintWriter out) {

        out.println("<h2>Liste des Controllers :</h2>");

        urlProcessor.getControllerClasses()
                .forEach(controller -> out.println("<p>" + controller + "</p>"));
    }

    private void printMappings(PrintWriter out) {

        out.println("<h2>Liste des Url :</h2>");

        urlProcessor.getUrlMapps()
                .forEach((key, value) -> out.println("<p>" + key + " : " + value + "</p>"));
    }

    private void printError(PrintWriter out, String message) {
        out.println("<p>" + message + "</p>");
    }
}