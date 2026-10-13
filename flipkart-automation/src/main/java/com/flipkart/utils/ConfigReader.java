package com.flipkart.utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Cannot read config.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key).trim();
    }
}
