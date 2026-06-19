package com.dirkfw.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.dirkfw.err.UrlNotSupportedException;

public class ControllerHandler {
    List<Class<?>> controllerClasses = new ArrayList<>();
    HashMap<String, UrlControllerMap> urlMapps = new HashMap<>();

    public ControllerHandler(List<Class<?>> controllerClasses, HashMap<String, UrlControllerMap> urlMapps) {
        this.controllerClasses = controllerClasses;
        this.urlMapps = urlMapps;
    }

    public List<Class<?>> getControllerClasses() {
        return controllerClasses;
    }

    public void setControllerClasses(List<Class<?>> controllerClasses) {
        this.controllerClasses = controllerClasses;
    }

    public HashMap<String, UrlControllerMap> getUrlMapps() {
        return urlMapps;
    }

    public void setUrlMapps(HashMap<String, UrlControllerMap> urlMapps) {
        this.urlMapps = urlMapps;
    }

    public void verifyvalidUrl(String url) throws UrlNotSupportedException {
        if(!this.getUrlMapps().containsKey(url)) throw new UrlNotSupportedException(url, urlMapps);
    }

}
