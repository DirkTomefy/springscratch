package com.dirkfw.exception;

import java.lang.reflect.Parameter;

public class FrontServletExecuterException extends DfwException {

    private static final long serialVersionUID = 1L;

    private static final int BAD_REQUEST = 400; 
    private static final int SERVER_ERROR = 500;

    public FrontServletExecuterException(int httpStatus, String message) {
        super(httpStatus, message);
    }

    public FrontServletExecuterException(int httpStatus, String message, Throwable cause) {
        super(httpStatus, message, cause);
    }


    public static FrontServletExecuterException missingParam(String name) {
        return new FrontServletExecuterException(BAD_REQUEST,
                "Paramètre requis manquant : " + name);
    }

    public static FrontServletExecuterException invalidValue(
            String name, String value, Throwable cause) {
        return new FrontServletExecuterException(BAD_REQUEST,
                "Valeur invalide pour le paramètre '" + name + "' : " + value, cause);
    }


    public static FrontServletExecuterException unsupportedParameter(Parameter parameter) {
        return new FrontServletExecuterException(SERVER_ERROR,
                "Paramètre non supporté : " + parameter.getType().getName()
                + " dans la méthode "
                + parameter.getDeclaringExecutable().getName());
    }

    public static FrontServletExecuterException unsupportedType(Class<?> type) {
        return new FrontServletExecuterException(SERVER_ERROR,
                "Type non supporté pour @RequestParam : " + type.getName());
    }

    public static FrontServletExecuterException springContextNotConfigured(Class<?> type) {
        return new FrontServletExecuterException(SERVER_ERROR,
                "Le contexte Spring n'est pas configuré pour le paramètre " + type.getName());
    }
}