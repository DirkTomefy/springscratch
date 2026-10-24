package com.dirkfw.exception;


public class DfwException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int httpStatus;

    public DfwException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public DfwException(int httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}