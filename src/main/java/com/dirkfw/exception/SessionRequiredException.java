package com.dirkfw.exception;

public class SessionRequiredException extends DfwException {

    private static final long serialVersionUID = 1L;

    private static final int STATUS = 401; // Unauthorized

    public SessionRequiredException(String message) {
        super(STATUS, message);
    }

    public SessionRequiredException(String message, Throwable cause) {
        super(STATUS, message, cause);
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