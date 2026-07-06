package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.dirkfw.classes.FrontServletParam;
import com.dirkfw.classes.helper.UrlHTTPMethod;
import com.dirkfw.classes.key.UrlKey;
import com.dirkfw.classes.mapping.ModelAndView;
import com.dirkfw.classes.mapping.UrlControllerMap;
import com.dirkfw.err.UrlNotSupportedException;
import com.dirkfw.servlet.listener.FrontServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServletController extends HttpServlet {

    String prefixOfView;
    String suffixOfView;
    private FrontServletParam urlProcessor;

    @Override
    public void init() throws ServletException {
        urlProcessor = (FrontServletParam) getServletContext()
                    .getAttribute(FrontServletContextListener.URL_PROCESSOR_ATTR);
        prefixOfView = this.getInitParameter("VIEW_PREFIX");
        suffixOfView = this.getInitParameter("VIEW_SUFFIX");
    }

    private void executeRequest(HttpServletRequest request)
            throws UrlNotSupportedException, ReflectiveOperationException {

        String urlString = getRequestedUrl(request);
        UrlHTTPMethod method = UrlHTTPMethod.buildUrlHTTPMethod(request.getMethod());
        UrlKey urlKey = new UrlKey(urlString, method);
        verifyIsValidUrl(urlKey) ;
        UrlControllerMap map = this.urlProcessor.getUrlMapps().get(urlKey);
        Object maybeModelAndView =map.getReflectMethod().invoke(map.getPrototypeSeed());
        if(maybeModelAndView instanceof ModelAndView){
            ModelAndView mav=(ModelAndView) maybeModelAndView;
            handleModelAndView(mav,request);
        }
    }

    private void verifyIsValidUrl(UrlKey urlKey) throws UrlNotSupportedException{
        if (!this.urlProcessor.getUrlMapps().containsKey(urlKey)) 
            throw new UrlNotSupportedException(urlKey, urlProcessor.getUrlMapps());

    }

    private void handleModelAndView(ModelAndView mav,HttpServletRequest request){
        for (Map.Entry<String,Object> attr : mav.getAttributes().entrySet()) { 
            request.setAttribute(attr.getKey(), attr.getValue());
        }
        request.getRequestDispatcher(this.prefixOfView+mav.getViewName()+this.suffixOfView);
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
            printError(out, e);
            e.printStackTrace();
            out.close();
        } 
    }


    private void printError(PrintWriter out, Exception e) {
        out.println(" <p >Une erreur interne du framework a été détéctée </p>");
        e.printStackTrace(out);
    }
}