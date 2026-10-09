package com.dirkfw.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.dirkfw.annotation.JsonResponse;
import com.dirkfw.annotation.ObjectParam;
import com.dirkfw.annotation.RequestParam;
import com.dirkfw.exception.FrontServletExecuterException;
import com.dirkfw.mapping.ModelAndView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServletExecuterHelper {

    private static final String SPRING_CONTEXT_CLASS = "org.springframework.context.ApplicationContext";

    private static final ObjectMapper JSON = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final Set<String> BLOCKED_FIELDS =
            Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("class")));

   
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

        if (isSimpleType(type))
            return readRequestParam(parameter, null, request);

        ObjectParam op = parameter.getAnnotation(ObjectParam.class);
        String prefix;

        if (op != null && !op.name().trim().isEmpty()) {
            prefix = op.name().trim();
        } else {
            prefix = parameter.getName();
            if (prefix == null || prefix.isEmpty() || prefix.startsWith("arg")) {
                System.err.println("[dirkfw] Nom de paramètre non fiable pour " + parameter
                        + " — ajoutez @ObjectParam(name=\"...\") "
                        + "ou compilez avec -parameters.");
            }
        }

        return bindObject(type, request, prefix);
    }

    
    private static Object readRequestParam(Parameter parameter, RequestParam rp,
            HttpServletRequest request) {

        String name;
        boolean required;

        if (rp == null) {
            name = parameter.getName();
            required = false;
        } else {
            String annotated = rp.name().trim();
            name = annotated.isEmpty() ? parameter.getName() : annotated;
            required = rp.isRequired();
        }

        String value = request.getParameter(name);

        if (value == null || value.trim().isEmpty()) {
            if (required)
                throw FrontServletExecuterException.missingParam(name);
            return defaultValue(parameter.getType());
        }
        return convert(value, parameter.getType(), name);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive())
            return null;
        if (type == boolean.class) return false;
        if (type == char.class)    return '\0';
        if (type == byte.class)    return (byte) 0;
        if (type == short.class)   return (short) 0;
        if (type == int.class)     return 0;
        if (type == long.class)    return 0L;
        if (type == float.class)   return 0f;
        if (type == double.class)  return 0d;
        return null;
    }

    private static Object convert(String value, Class<?> type, String name) {
        try {
            if (type == String.class)                            return value;
            if (type == int.class     || type == Integer.class)  return Integer.parseInt(value);
            if (type == long.class    || type == Long.class)     return Long.parseLong(value);
            if (type == double.class  || type == Double.class)   return Double.parseDouble(value);
            if (type == float.class   || type == Float.class)    return Float.parseFloat(value);
            if (type == boolean.class || type == Boolean.class)  return Boolean.parseBoolean(value);
            if (type == short.class   || type == Short.class)    return Short.parseShort(value);
            if (type == byte.class    || type == Byte.class)     return Byte.parseByte(value);

            if (type == char.class || type == Character.class) {
                if (value.length() != 1)
                    throw FrontServletExecuterException.invalidValue(
                            name, value, new IllegalArgumentException("un seul caractere attendu"));
                return value.charAt(0);
            }

            if (type == BigDecimal.class) return new BigDecimal(value);
            if (type == BigInteger.class) return new BigInteger(value);

            if (type == LocalDate.class)     return LocalDate.parse(value);
            if (type == LocalTime.class)     return LocalTime.parse(value);
            if (type == LocalDateTime.class) return LocalDateTime.parse(value);

        } catch (NumberFormatException e) {
            throw FrontServletExecuterException.invalidValue(name, value, e);
        } catch (DateTimeParseException e) {
            throw FrontServletExecuterException.invalidValue(name, value, e);
        }
        throw FrontServletExecuterException.unsupportedType(type);
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
            || type == String.class
            || type == Integer.class || type == Long.class
            || type == Double.class  || type == Float.class
            || type == Boolean.class || type == Short.class
            || type == Byte.class    || type == Character.class
            || type == BigDecimal.class || type == BigInteger.class
            || type == LocalDate.class  || type == LocalTime.class || type == LocalDateTime.class
            || type.isEnum();
    }

   
    private static Object bindObject(Class<?> type, HttpServletRequest request, String prefix) {
        Object instance;
        try {
            instance = type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Impossible d'instancier " + type.getName()
                            + " : constructeur sans argument manquant ?", e);
        }

        Map<String, String> flat = toFlatMap(request.getParameterMap());

        if (prefix != null && !prefix.isEmpty()) {
            flat = stripPrefix(flat, prefix);
        }

        Map<String, Map<String, String>> grouped = groupByFirstSegment(flat);
        applyGrouped(instance, type, grouped);
        return instance;
    }

    private static Map<String, String> stripPrefix(Map<String, String> flat, String prefix) {
        String p = prefix + ".";
        Map<String, String> out = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String> e : flat.entrySet()) {
            if (e.getKey().startsWith(p)) {
                out.put(e.getKey().substring(p.length()), e.getValue());
            }
        }
        return out;
    }

    private static Map<String, String> toFlatMap(Map<String, String[]> params) {
        Map<String, String> flat = new LinkedHashMap<String, String>();
        for (Map.Entry<String, String[]> e : params.entrySet()) {
            String[] v = e.getValue();
            if (v != null && v.length > 0 && v[0] != null && !v[0].trim().isEmpty()) {
                flat.put(e.getKey(), v[0]);
            }
        }
        return flat;
    }

    private static Map<String, Map<String, String>> groupByFirstSegment(Map<String, String> flat) {
        Map<String, Map<String, String>> grouped =
                new LinkedHashMap<String, Map<String, String>>();
        for (Map.Entry<String, String> e : flat.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            int dot = key.indexOf('.');
            if (dot < 0) {
                Map<String, String> bucket = grouped.get("");
                if (bucket == null) {
                    bucket = new LinkedHashMap<String, String>();
                    grouped.put("", bucket);
                }
                bucket.put(key, value);
            } else {
                String prefix = key.substring(0, dot);
                String rest   = key.substring(dot + 1);
                Map<String, String> bucket = grouped.get(prefix);
                if (bucket == null) {
                    bucket = new LinkedHashMap<String, String>();
                    grouped.put(prefix, bucket);
                }
                bucket.put(rest, value);
            }
        }
        return grouped;
    }

    private static void applyGrouped(Object target, Class<?> type,
            Map<String, Map<String, String>> grouped) {

        for (Map.Entry<String, Map<String, String>> group : grouped.entrySet()) {
            String prefix = group.getKey();
            Map<String, String> subValues = group.getValue();

            if (prefix.isEmpty()) {
                for (Map.Entry<String, String> e : subValues.entrySet()) {
                    setProperty(target, type, e.getKey(), e.getValue());
                }
            } else {
                Object sub = getOrCreateSubObject(target, type, prefix);
                if (sub == null) continue;
                applyGrouped(sub, sub.getClass(), groupByFirstSegment(subValues));
            }
        }
    }

    private static void setProperty(Object instance, Class<?> type,
            String fieldName, String rawValue) {

        if (BLOCKED_FIELDS.contains(fieldName)) return;
        if (fieldName.isEmpty()) return;

        String setterName = "set"
                + Character.toUpperCase(fieldName.charAt(0))
                + fieldName.substring(1);

        for (Method m : type.getMethods()) {
            if (!m.getName().equals(setterName) || m.getParameterCount() != 1) continue;

            Class<?> paramType = m.getParameterTypes()[0];
            if (!isSimpleType(paramType)) return;

            try {
                Object converted = convert(rawValue, paramType, fieldName);
                m.invoke(instance, converted);
            } catch (Exception e) {
                throw FrontServletExecuterException.invalidValue(fieldName, rawValue, e);
            }
            return;
        }
    }

    private static Object getOrCreateSubObject(Object parent, Class<?> parentType, String fieldName) {

        if (fieldName.isEmpty()) return null;

        String cap = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String getterName = "get" + cap;
        String setterName = "set" + cap;

        Method getter = null;
        Method setter = null;

        for (Method m : parentType.getMethods()) {
            if (m.getName().equals(getterName) && m.getParameterCount() == 0) getter = m;
            if (m.getName().equals(setterName) && m.getParameterCount() == 1) setter = m;
        }

        Class<?> subType = null;
        Object subInstance = null;

        if (getter != null && getter.getReturnType() != void.class
                && !isSimpleType(getter.getReturnType())) {
            subType = getter.getReturnType();
            try {
                subInstance = getter.invoke(parent);
            } catch (Exception ignored) {
            }
        }

        if (subType == null && setter != null) {
            subType = setter.getParameterTypes()[0];
        }

        if (subType == null || isSimpleType(subType)) return null;

        if (subInstance == null) {
            try {
                subInstance = subType.getDeclaredConstructor().newInstance();
                if (setter != null) setter.invoke(parent, subInstance);
            } catch (Exception e) {
                return null;
            }
        }
        return subInstance;
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