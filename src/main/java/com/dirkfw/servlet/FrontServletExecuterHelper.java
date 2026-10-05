package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import com.dirkfw.annotation.JsonResponse;
import com.dirkfw.annotation.RequestParam;
import com.dirkfw.exception.FrontServletExecuterException;
import com.dirkfw.mapping.ModelAndView;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FrontServletExecuterHelper {

    private static final String SPRING_CONTEXT_CLASS = "org.springframework.context.ApplicationContext";
    private static final ObjectMapper JSON = new ObjectMapper();

    public static Object[] resolveArguments(FrontServletController controller, Method method,
            HttpServletRequest request, HttpServletResponse response) {

        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        Class<?> springContext = loadSpringContext();

        for (int i = 0; i < parameters.length; i++) {
            args[i] = resolveArgument(parameters[i], controller, request, response, springContext);
        }
        return args;
    }

    private static Object resolveArgument(Parameter parameter, FrontServletController controller,
            HttpServletRequest request, HttpServletResponse response, Class<?> springContext) {

        RequestParam rp = parameter.getAnnotation(RequestParam.class);
        if (rp != null)
            return readRequestParam(parameter, rp, request);

        Class<?> type = parameter.getType();
        if (type == HttpServletRequest.class)
            return request;
        if (type == HttpServletResponse.class)
            return response;

        if (springContext != null && springContext.isAssignableFrom(type)) {
            Object ctx = controller.frontServletParam.getExternalContext();
            if (ctx == null)
                throw FrontServletExecuterException.springContextNotConfigured(type);
            return ctx;
        }

        return readRequestParam(parameter, null, request);
    }

    private static Object readRequestParam(Parameter parameter, RequestParam rp,
            HttpServletRequest request) {

        String name;
        boolean required;

        if (rp == null) {
            name = parameter.getName();
            required = false;
        } else {
            name = rp.name().isBlank() ? parameter.getName() : rp.name();
            required = rp.isRequired();
        }

        String value = request.getParameter(name);

        if (value == null) {
            if (required)
                throw FrontServletExecuterException.missingParam(name);
            return defaultValue(parameter.getType());
        }
        return convert(value, parameter.getType(), name);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive())
            return null;
        if (type == boolean.class)
            return false;
        if (type == char.class)
            return '\0';
        if (type == byte.class)
            return (byte) 0;
        if (type == short.class)
            return (short) 0;
        if (type == int.class)
            return 0;
        if (type == long.class)
            return 0L;
        if (type == float.class)
            return 0f;
        if (type == double.class)
            return 0d;
        return null;
    }

    private static Object convert(String value, Class<?> type, String name) {
        try {
            if (type == String.class)
                return value;
            if (type == int.class || type == Integer.class)
                return Integer.parseInt(value);
            if (type == long.class || type == Long.class)
                return Long.parseLong(value);
            if (type == double.class || type == Double.class)
                return Double.parseDouble(value);
            if (type == float.class || type == Float.class)
                return Float.parseFloat(value);
            if (type == boolean.class || type == Boolean.class)
                return Boolean.parseBoolean(value);
            if (type == short.class || type == Short.class)
                return Short.parseShort(value);
            if (type == byte.class || type == Byte.class)
                return Byte.parseByte(value);

            if (type == char.class || type == Character.class) {
                if (value.length() != 1)
                    throw FrontServletExecuterException.invalidValue(
                            name, value, new IllegalArgumentException("un seul caractère attendu"));
                return value.charAt(0);
            }

            if (type == BigDecimal.class)
                return new BigDecimal(value);
            if (type == BigInteger.class)
                return new BigInteger(value);

        } catch (NumberFormatException e) {
            throw FrontServletExecuterException.invalidValue(name, value, e);
        }
        throw FrontServletExecuterException.unsupportedType(type);
    }

    public static void forwardToView(FrontServletController controller, ModelAndView mav,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        for (Map.Entry<String, Object> attr : mav.getAttributes().entrySet()) {
            request.setAttribute(attr.getKey(), attr.getValue());
        }
        String path = controller.prefixOfView + mav.getViewName() + controller.suffixOfView;
        request.getRequestDispatcher(path).forward(request, response);
    }

    public static void writeJson(Method method, Object returnValue,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        JsonResponse annotation = method.getAnnotation(JsonResponse.class);
        String json = annotation.isRawString()
                ? String.valueOf(returnValue)
                : JSON.writeValueAsString(returnValue);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }

    private static Class<?> loadSpringContext() {
        try {
            return Class.forName(SPRING_CONTEXT_CLASS);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}