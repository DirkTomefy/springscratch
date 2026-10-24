package com.dirkfw.exception;

import java.util.Collections;
import java.util.Map;

import com.dirkfw.mapping.UrlControllerMap;
import com.dirkfw.mapping.UrlKey;


public class UrlNotSupportedException extends DfwException {

    private static final long serialVersionUID = 1L;

    private static final int NOT_FOUND = 404;

    private final transient UrlKey urlGot;
    private final transient Map<UrlKey, UrlControllerMap> supportedUrl;

    public UrlNotSupportedException(UrlKey urlGot, Map<UrlKey, UrlControllerMap> supportedUrl) {
        super(NOT_FOUND, "URL non supportée : " + urlGot);
        this.urlGot = urlGot;
        this.supportedUrl = supportedUrl;
    }

    public UrlKey getUrlGot() {
        return urlGot;
    }

    public Map<UrlKey, UrlControllerMap> getSupportedUrl() {
        return supportedUrl == null
                ? Collections.<UrlKey, UrlControllerMap>emptyMap()
                : Collections.unmodifiableMap(supportedUrl);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("L'URL \"")
          .append(urlGot)
          .append("\" n'est pas supportée.\n\n");

        sb.append("URLs supportées :\n");

        for (UrlKey url : getSupportedUrl().keySet()) {
            sb.append(" - ")
              .append(url)
              .append(";\n");
        }

        return sb.toString();
    }
}