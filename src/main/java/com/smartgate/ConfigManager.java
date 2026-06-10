package com.smartgate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private static final Map<String, String> config = new HashMap<>();

    static {
        load();
    }

    private static void load() {
        System.out.println("Çalışma dizini: " + System.getProperty("user.dir"));
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    config.put(parts[0].trim(), parts[1].trim());
                }
            }
            System.out.println("Config yüklendi.");
        } catch (IOException e) {
            System.err.println("env dosyası bulunamadı, default değerler kullanılıyor.");
        }
    }

    public static String get(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }
}