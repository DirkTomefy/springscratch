package com.dirkfw.classes.key;

import com.dirkfw.classes.helper.UrlHTTPMethod;

public class UrlKey {
    
    String urlString;
    UrlHTTPMethod methodHttp;


    public UrlKey(String urlString, UrlHTTPMethod methodHttp) {
        this.urlString = urlString;
        this.methodHttp = methodHttp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((urlString == null) ? 0 : urlString.hashCode());
        result = prime * result + ((methodHttp == null) ? 0 : methodHttp.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UrlKey other = (UrlKey) obj;
        if (urlString == null) {
            if (other.urlString != null)
                return false;
        } else if (!urlString.equals(other.urlString))
            return false;
        if (methodHttp != other.methodHttp)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "UrlKey [urlString=" + urlString + ", methodHttp=" + methodHttp + "]";
    }
    
}
