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

    private static final String OLLAMA_HOST = "http://" + ConfigManager.get("OLLAMA_HOST", "10.194.166.29") + ":11434";
    private static final String MODEL = "gemma3:4b";
    private final Gson gson = new Gson();

    public String chat(String prompt) {
        try {
            URL url = new URL(OLLAMA_HOST + "/api/generate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);

            JsonObject body = new JsonObject();
            body.addProperty("model", MODEL);
            body.addProperty("prompt", prompt);
            body.addProperty("stream", false);

            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
                writer.write(gson.toJson(body));
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

            JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
            return jsonResponse.get("response").getAsString();

        } catch (Exception e) {
            System.err.println("Ollama bağlantı hatası: " + e.getMessage());
            return "Ollama bağlantısı kurulamadı.";
        }
    }
}