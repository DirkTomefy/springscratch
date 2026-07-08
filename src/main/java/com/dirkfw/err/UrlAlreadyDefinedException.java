package com.dirkfw.err;


import com.dirkfw.classes.key.UrlKey;
import com.dirkfw.classes.mapping.UrlControllerMap;

public class UrlAlreadyDefinedException extends Exception {
    UrlKey url;
    UrlControllerMap value;
    public UrlAlreadyDefinedException(UrlKey url, UrlControllerMap value) {
        super("Url "+url+" est déja définie pour la valeur "+value);
        this.url = url;
        this.value = value;
    }
}