package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import com.dirkfw.annotation.JsonResponse;

import com.dirkfw.mapping.ModelAndView;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FrontServletExecuterHelper {
    public static Object[] buildMethodArguments(FrontServletController controller, Method method,
            HttpServletRequest request, HttpServletResponse response) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        Class<?> applicationContextClass = null;
        try {
            applicationContextClass = Class.forName("org.springframework.context.ApplicationContext");
        } catch (ClassNotFoundException ignored) {
        }

        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = parameters[i].getType();

            if (paramType == HttpServletRequest.class) {
                args[i] = request;
            } else if (paramType == HttpServletResponse.class) {
                args[i] = response;
            } else if (applicationContextClass != null && applicationContextClass.isAssignableFrom(paramType)) {
                args[i] = controller.frontServletParam.getExternalContext();
                if (args[i] == null) {
                    throw new IllegalArgumentException(
                            "Le paramètre " + paramType.getName()
                                    + " est demandé mais le contexte Spring n'est pas configuré.");
                }
            } else {
                throw new IllegalArgumentException(
                        "Paramètre de type non supporté : " + paramType.getName()
                                + " dans la méthode " + method.getName());
            }
        }
        return args;
    }

    public static void handleModelAndView(FrontServletController controller, ModelAndView mav,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        for (Map.Entry<String, Object> attr : mav.getAttributes().entrySet()) {
            request.setAttribute(attr.getKey(), attr.getValue());
        }
        String path = controller.prefixOfView + mav.getViewName() + controller.suffixOfView;

        request.getRequestDispatcher(path).forward(request, response);
    }

    public static void handleJsonResponse(Method method,
            Object returnValue,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        JsonResponse annotation = method.getAnnotation(JsonResponse.class);
        String json;
        if (annotation.isRawString()) {
            json = String.valueOf(returnValue);
        } else {
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(returnValue);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        try (PrintWriter out = response.getWriter()) {
            out.write(json);
            out.flush();
        }
    }

    
}
