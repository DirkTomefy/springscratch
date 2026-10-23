package com.dirkfw.exception;

public class SessionRequiredException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SessionRequiredException(String message) {
        super(message);
    }

    public SessionRequiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public static SessionRequiredException noSession(String methodName) {
        return new SessionRequiredException(
                "Aucune session active pour " + methodName);
    }

    public static SessionRequiredException missingAttribute(String name) {
        return new SessionRequiredException(
                "Attribut de session requis manquant : '" + name + "'");
    }

    public static SessionRequiredException wrongType(
            String name, Class<?> expected, Class<?> actual) {
        return new SessionRequiredException(
                "Attribut '" + name + "' : type attendu " + expected.getName()
                + ", trouvé : " + (actual == null ? "null" : actual.getName()));
    }
}