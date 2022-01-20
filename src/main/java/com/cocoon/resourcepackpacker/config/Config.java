package com.cocoon.resourcepackpacker.config;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class Config {
    public static Properties projectProperties = new Properties();
    public static Properties jarProperties = new Properties();

    private static String jarConfigFilePath;
    private static String projectConfigFilePath;

    public static void loadCustomProperties(Properties properties, Path path, String[] defaultProperties) {
        try {
            if (path == null || path.toString().equals("null")) {return;}
            String configFilePath = path + "/rpp.config";
            if (properties.equals(jarProperties)) {
                jarConfigFilePath = configFilePath;
            } else {
                projectConfigFilePath = configFilePath;
            }
            // Load the config or generate if not available
            File configFile = new File(configFilePath);
            if (configFile.createNewFile()) {
                for (String p: defaultProperties) {
                    properties.setProperty(p, "null");
                }
                save(properties);
            }

            FileInputStream propertyIS = new FileInputStream(configFilePath);
            properties.load(propertyIS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isInArrayProperty(Properties properties, String property, String value) {
        return properties.getProperty(property).contains(value);
    }

    public static void arrayPropertyAdd(Properties properties, String property, String value) {
        // add the value if it's not already there
        String propertyList = properties.getProperty(property);
        if (propertyList == null || !propertyList.contains(value)) {
            properties.setProperty(property, propertyList+","+value);
        }
        save(properties);
    }

    public static void arrayPropertyRemove(Properties properties, String property, String value) {
        // remove the value if it is there
        String propertyList = properties.getProperty(property);
        if (propertyList != null && propertyList.contains(value)) {
            properties.setProperty(property, propertyList.replace(","+value, ""));
        }
        save(properties);
    }

    public static void save(Properties properties, Integer commentsId) {
        String[] comments = new String[]{"Config File for Resource Pack Packer (keep in same level as pack.mcmeta)",
                "Config File for Resource Pack Packer (keep in same level as the JAR file)"};

        try {
            String configFilePath;
            if (properties.equals(jarProperties)) {
                configFilePath = jarConfigFilePath;
            } else {
                configFilePath = projectConfigFilePath;
            }
            if (configFilePath == null) {return;}
            Writer PropWriter = Files.newBufferedWriter(Paths.get(configFilePath));
            properties.store(PropWriter, comments[commentsId]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(Properties properties) {

        save(properties, 0);
    }
}
