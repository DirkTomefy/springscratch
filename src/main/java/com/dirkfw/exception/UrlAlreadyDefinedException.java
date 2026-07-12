package com.dirkfw.exception;


import com.dirkfw.mapping.UrlKey;
import com.dirkfw.mapping.UrlControllerMap;

public class UrlAlreadyDefinedException extends Exception {
    UrlKey url;
    UrlControllerMap value;
    public UrlAlreadyDefinedException(UrlKey url, UrlControllerMap value) {
        super("Url "+url+" est déja définie pour la valeur "+value);
        this.url = url;
        this.value = value;
    }
}