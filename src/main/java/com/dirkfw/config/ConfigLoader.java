package com.dirkfw.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    public static final String DIRKFW_PROPERTIES_FILE  = "dirkfw.propreties";
    public static final String UPLOAD_DIR_KEY          = "dirkfw.updir";

    private final Properties properties = new Properties();

    public ConfigLoader(String fileName) {
        try (InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream(fileName)) {

            if (is == null) {
                throw new RuntimeException("Fichier introuvable : " + fileName);
            }
            properties.load(is);

        } catch (IOException e) {
            throw new RuntimeException("Erreur de chargement : " + fileName, e);
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String val = properties.getProperty(key);
        return val != null ? Integer.parseInt(val) : defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String val = properties.getProperty(key);
        return val != null ? Boolean.parseBoolean(val) : defaultValue;
    }
}