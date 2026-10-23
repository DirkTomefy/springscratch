package com.dirkfw.session;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

public class DfwSession {

    public static final String CURRENT_ATTR = "__DFW_SESSION__";

    private final HttpSession http;
    private final Map<String, Object> attrs = new LinkedHashMap<>();
    private final Set<String> initial = new LinkedHashSet<>();

    public DfwSession(HttpSession http) {
        this.http = http;
    }

    public HttpSession getHttp()      { return http; }
    public String getId()             { return http.getId(); }
    public long   getCreationTime()   { return http.getCreationTime(); }
    public long   getLastAccessTime() { return http.getLastAccessedTime(); }

    public void refresh() {
        attrs.clear();
        initial.clear();
        Enumeration<String> names = http.getAttributeNames();
        while (names.hasMoreElements()) {
            String n = names.nextElement();
            attrs.put(n, http.getAttribute(n));
            initial.add(n);
        }
    }

    public void flush() {
        for (String k : initial) {
            if (!attrs.containsKey(k)) http.removeAttribute(k);
        }
        for (Map.Entry<String, Object> e : attrs.entrySet()) {
            http.setAttribute(e.getKey(), e.getValue());
        }
    }

    public Object getAttribute(String name) {
        return attrs.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name, Class<T> type) {
        Object v = attrs.get(name);
        if (v == null) return null;
        if (!type.isInstance(v)) {
            throw new IllegalStateException(
                    "Attribut '" + name + "' est de type "
                    + v.getClass().getName() + ", attendu : " + type.getName());
        }
        return (T) v;
    }

    public void setAttribute(String name, Object value) {
        if (name == null) throw new IllegalArgumentException("name required");
        initial.add(name);
        if (value == null) { attrs.remove(name); return; }
        attrs.put(name, value);
    }

    public void removeAttribute(String name) {
        attrs.remove(name);
    }

    public boolean hasAttribute(String name) {
        return attrs.containsKey(name);
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attrs);
    }

    public Set<String> getAttributeNames() {
        return Collections.unmodifiableSet(attrs.keySet());
    }

    public void clear() {
        attrs.clear();
    }

    public void invalidate() {
        http.invalidate();
    }
}