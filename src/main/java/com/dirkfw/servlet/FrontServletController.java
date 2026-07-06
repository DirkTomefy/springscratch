package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import com.dirkfw.classes.FrontServletParam;
import com.dirkfw.classes.helper.UrlHTTPMethod;
import com.dirkfw.classes.key.UrlKey;
import com.dirkfw.classes.mapping.UrlControllerMap;
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

        String urlString = getRequestedUrl(request);
        UrlHTTPMethod method = UrlHTTPMethod.buildUrlHTTPMethod(request.getMethod());
        UrlKey urlKey = new UrlKey(urlString, method);
        verifyIsValidUrl(urlKey) ;
        UrlControllerMap map = this.urlProcessor.getUrlMapps().get(urlKey);
        map.getReflectMethod().invoke(map.getPrototypeSeed());
    }

    private void verifyIsValidUrl(UrlKey urlKey) throws UrlNotSupportedException{
        if (!this.urlProcessor.getUrlMapps().containsKey(urlKey)) 
            throw new UrlNotSupportedException(urlKey, urlProcessor.getUrlMapps());

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

        try {
            executeRequest(request);
        } catch (Exception e) {
            PrintWriter out = response.getWriter();
            printError(out, e.toString());
            e.printStackTrace();
            out.close();
        } 
    }


    private void printError(PrintWriter out, String message) {
        out.println("<p>" + message + "</p>");
    }
}