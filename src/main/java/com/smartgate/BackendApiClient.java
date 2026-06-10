package com.smartgate;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class BackendApiClient {

    private final String baseUrl;
    private final Gson gson = new Gson();

    public BackendApiClient() {
        String host = ConfigManager.get("BACKEND_HOST", "192.168.36.29");
        String port = ConfigManager.get("BACKEND_PORT", "8081");
        this.baseUrl = "http://" + host + ":" + port + "/api";
    }

    public String get(String endpoint) {
        try {
            URL url = new URL(baseUrl + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } catch (Exception e) {
            System.err.println("Backend API hatası: " + e.getMessage());
            return null;
        }
    }

    public String post(String endpoint, String jsonBody) {
        try {
            URL url = new URL(baseUrl + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);

            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(jsonBody);
                writer.flush();
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } catch (Exception e) {
            System.err.println("Backend API POST hatası: " + e.getMessage());
            return null;
        }
    }

    public boolean unlockDoor(String deviceId) {
        String body = "{\"deviceId\":\"" + deviceId + "\",\"method\":\"CONSOLE\"}";
        String response = post("/door/unlock", body);
        return response != null && response.contains("success");
    }
}