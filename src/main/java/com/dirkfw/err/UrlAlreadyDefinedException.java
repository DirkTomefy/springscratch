package com.dirkfw.err;


import com.dirkfw.mapping.UrlControllerMap;
import com.dirkfw.mapping.UrlKey;

public class UrlAlreadyDefinedException extends Exception {
    UrlKey url;
    UrlControllerMap value;
    public UrlAlreadyDefinedException(UrlKey url, UrlControllerMap value) {
        super("Url "+url+" est déja définie pour la valeur "+value);
        this.url = url;
        this.value = value;
    }
}