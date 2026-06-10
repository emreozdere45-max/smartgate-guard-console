package com.smartgate.llm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smartgate.ConfigManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class OllamaClient {

    private String getOllamaUrl() {
        return "http://" + ConfigManager.get("OLLAMA_HOST", "192.168.36.29") + ":11434";
    }

    private static final String MODEL = ConfigManager.get("OLLAMA_MODEL", "gemma4:e4b");
    private final Gson gson = new Gson();

    public String chat(String prompt) {
        try {
            String fullUrl = getOllamaUrl() + "/api/generate";
            System.out.println("Ollama URL: " + fullUrl);
            System.out.println("Model: " + MODEL);

            URL url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(120000);

            JsonObject body = new JsonObject();
            body.addProperty("model", MODEL);
            body.addProperty("prompt", prompt);
            body.addProperty("stream", false);

            String jsonBody = gson.toJson(body);
            System.out.println("Gönderilen body: " + jsonBody);

            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(jsonBody);
                writer.flush();
            }

            int responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
            return jsonResponse.get("response").getAsString();

        } catch (Exception e) {
            System.err.println("Ollama bağlantı hatası: " + e.getClass().getName() + " - " + e.getMessage());
            return "Ollama bağlantısı kurulamadı.";
        }
    }
}