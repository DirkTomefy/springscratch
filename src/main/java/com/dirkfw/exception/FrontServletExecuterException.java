package com.dirkfw.exception;

public class FrontServletExecuterException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FrontServletExecuterException(String message) {
        super(message);
    }

    public FrontServletExecuterException(String message, Throwable cause) {
        super(message, cause);
    }


    public static FrontServletExecuterException missingParam(String name) {
        return new FrontServletExecuterException(
                "Paramètre requis manquant : " + name);
    }

    public static FrontServletExecuterException unsupportedParameter(
            java.lang.reflect.Parameter parameter) {
        return new FrontServletExecuterException(
                "Paramètre non supporté : " + parameter.getType().getName()
                        + " dans la méthode "
                        + parameter.getDeclaringExecutable().getName());
    }

    public static FrontServletExecuterException invalidValue(
            String name, String value, Throwable cause) {
        return new FrontServletExecuterException(
                "Valeur invalide pour le paramètre '" + name + "' : " + value, cause);
    }

    public static FrontServletExecuterException unsupportedType(Class<?> type) {
        return new FrontServletExecuterException(
                "Type non supporté pour @RequestParam : " + type.getName());
    }

    public static FrontServletExecuterException springContextNotConfigured(Class<?> type) {
        return new FrontServletExecuterException(
                "Le contexte Spring n'est pas configuré pour le paramètre " + type.getName());
    }
}