package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;


import com.dirkfw.annotation.JsonResponse;
import com.dirkfw.core.FrontServletParam;
import com.dirkfw.mapping.UrlHTTPMethod;
import com.dirkfw.mapping.UrlKey;
import com.dirkfw.mapping.ModelAndView;
import com.dirkfw.mapping.UrlControllerMap;
import com.dirkfw.exception.UrlNotSupportedException;
import com.dirkfw.servlet.listener.FrontServletContextListener;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServletController extends HttpServlet {

    String prefixOfView;
    String suffixOfView;
    FrontServletParam frontServletParam;

    @Override
    public void init() throws ServletException {
        frontServletParam = (FrontServletParam) getServletContext()
                .getAttribute(FrontServletContextListener.URL_PROCESSOR_ATTR);

        prefixOfView = (String) this.getServletContext().getAttribute(FrontServletContextListener.VIEW_PREFIX);
        suffixOfView = (String) this.getServletContext().getAttribute(FrontServletContextListener.VIEW_SUFFIX);
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
        verifyIsValidUrl(urlKey);
        UrlControllerMap map = frontServletParam.getUrlMapps().get(urlKey);
        Object controller = map.getControllerInstance(request);
        Method controllerMethod = map.getReflectMethod();

        Object[] args = FrontServletExecuterHelper.buildMethodArguments(this, controllerMethod, request, response);
        Object returnValueObject = controllerMethod.invoke(controller, args);

        if (returnValueObject == null)
            return;

        if (controllerMethod.isAnnotationPresent(JsonResponse.class)) {
            FrontServletExecuterHelper.handleJsonResponse(controllerMethod, returnValueObject, request, response);
        } else if (returnValueObject instanceof ModelAndView) {
            ModelAndView mav = (ModelAndView) returnValueObject;
            FrontServletExecuterHelper.handleModelAndView(this, mav, request, response);
        }
    }

    private void verifyIsValidUrl(UrlKey urlKey) throws UrlNotSupportedException {
        if (!this.frontServletParam.getUrlMapps().containsKey(urlKey))
            throw new UrlNotSupportedException(urlKey, frontServletParam.getUrlMapps());

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
            executeRequest(request, response);
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