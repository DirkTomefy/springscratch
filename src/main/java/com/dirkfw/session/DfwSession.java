package com.dirkfw.session;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

public class DfwSession {

    private final String id;
    private final long creationTime;
    private long lastAccessTime;
    private final Map<String, Object> attributes;
    private final Set<String> initialKeys;

    public DfwSession(String id) {
        this.id = id;
        long now = System.currentTimeMillis();
        this.creationTime = now;
        this.lastAccessTime = now;
        this.attributes = new LinkedHashMap<>();
        this.initialKeys = new LinkedHashSet<>();
    }

    private DfwSession(String id, long creationTime, long lastAccessTime) {
        this.id = id;
        this.creationTime = creationTime;
        this.lastAccessTime = lastAccessTime;
        this.attributes = new LinkedHashMap<>();
        this.initialKeys = new LinkedHashSet<>();
    }

    public static DfwSession from(HttpSession session) {
        DfwSession dfw = new DfwSession(
                session.getId(),
                session.getCreationTime(),
                session.getLastAccessedTime()
        );
        Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            dfw.attributes.put(name, session.getAttribute(name));
            dfw.initialKeys.add(name);
        }
        return dfw;
    }

    public String getId() { return id; }
    public long getCreationTime() { return creationTime; }
    public long getLastAccessTime() { return lastAccessTime; }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name, Class<T> type) {
        Object value = attributes.get(name);
        if (value == null) return null;
        if (!type.isInstance(value)) {
            throw new IllegalStateException(
                    "Attribut '" + name + "' est de type "
                    + value.getClass().getName()
                    + ", attendu : " + type.getName());
        }
        return (T) value;
    }

    public void setAttribute(String name, Object value) {
        if (name == null) throw new IllegalArgumentException("name required");
        if (value == null) { attributes.remove(name); return; }
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    public void touch() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    public void clear() {
        attributes.clear();
    }

    public void flushTo(HttpSession httpSession) {
        for (String key : initialKeys) {
            if (!attributes.containsKey(key)) {
                httpSession.removeAttribute(key);
            }
        }
        for (Map.Entry<String, Object> e : attributes.entrySet()) {
            httpSession.setAttribute(e.getKey(), e.getValue());
        }
        this.lastAccessTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "DfwSession[id=" + id
                + ", creationTime=" + creationTime
                + ", lastAccessTime=" + lastAccessTime
                + ", attributes=" + attributes.keySet() + "]";
    }
}