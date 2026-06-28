package com.dirkfw.err;

import java.util.HashMap;

import com.dirkfw.mapping.UrlControllerMap;

public class UrlNotSupportedException extends Exception {
    String urlGot;
    HashMap<String, UrlControllerMap> supportedUrl;

    public UrlNotSupportedException(String urlGot, HashMap<String, UrlControllerMap> supportedUrl) {
        super("URL non supportée : " + urlGot);
        this.urlGot = urlGot;
        this.supportedUrl = supportedUrl;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("L'URL \"")
          .append(urlGot)
          .append("\" n'est pas supportée.\n\n");

        sb.append("URLs supportées :\n");

        for (String url : supportedUrl.keySet()) {
            sb.append(" - ")
              .append(url)
              .append("; \n");
        }

        return sb.toString();
    }
}