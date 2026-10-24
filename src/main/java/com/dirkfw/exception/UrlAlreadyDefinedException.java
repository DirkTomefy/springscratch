package com.dirkfw.exception;

import com.dirkfw.mapping.UrlControllerMap;
import com.dirkfw.mapping.UrlKey;


public class UrlAlreadyDefinedException extends DfwException {

    private static final long serialVersionUID = 1L;

    private final transient UrlKey url;
    private final transient UrlControllerMap value;

    public UrlAlreadyDefinedException(UrlKey url, UrlControllerMap value) {
        super(500, "Url " + url + " est déjà définie pour la valeur " + value);
        this.url = url;
        this.value = value;
    }

    public UrlKey getUrl() {
        return url;
    }

    public UrlControllerMap getValue() {
        return value;
    }
}