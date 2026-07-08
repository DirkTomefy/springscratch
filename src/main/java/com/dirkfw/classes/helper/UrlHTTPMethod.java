package com.dirkfw.classes.helper;

public enum UrlHTTPMethod {
    GET,
    POST;

    public static UrlHTTPMethod buildUrlHTTPMethod(String method) {
        if ("GET".equalsIgnoreCase(method)) {
            return UrlHTTPMethod.GET;
        } else if ("POST".equalsIgnoreCase(method)) {
            return UrlHTTPMethod.POST;
        } else {
            return UrlHTTPMethod.GET;
        }
    }
}
