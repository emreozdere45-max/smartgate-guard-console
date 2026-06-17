package com.smartgate;

import com.google.gson.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.smartgate.model.Visitor;
import com.smartgate.model.Device;
import com.smartgate.model.Resident;

public class BackendApiClient {

    private final String baseUrl;
    private final Gson gson = new Gson();

    public BackendApiClient() {
        String host = ConfigManager.get("BACKEND_HOST", "localhost");
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
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
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(jsonBody);
                writer.flush();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();
            return response.toString();
        } catch (Exception e) {
            System.err.println("Backend API POST hatası: " + e.getMessage());
            return null;
        }
    }

    public String put(String endpoint) {
        try {
            URL url = new URL(baseUrl + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();
            return response.toString();
        } catch (Exception e) {
            System.err.println("Backend API PUT hatası: " + e.getMessage());
            return null;
        }
    }

    public boolean unlockDoor(String deviceId) {
        String body = "{\"deviceId\":\"" + deviceId + "\",\"method\":\"CONSOLE\"}";
        String response = post("/door/unlock", body);
        return response != null && response.contains("success");
    }

    public boolean unlockDeviceDoor(Long deviceId) {
        String response = post("/devices/" + deviceId + "/door/unlock", "{}");
        return response != null && response.contains("success");
    }

    public List<Device> getDevices() {
        String response = get("/devices");
        if (response == null || response.isBlank()) return Collections.emptyList();
        Device[] devices = gson.fromJson(response, Device[].class);
        return devices == null ? Collections.emptyList() : Arrays.asList(devices);
    }

    public Device createDevice(String name, String ipAddress, int port, String location, String macAddress) {
        JsonObject body = new JsonObject();
        body.addProperty("name", name);
        body.addProperty("ipAddress", ipAddress);
        body.addProperty("commandPort", port);
        body.addProperty("location", location != null ? location : "");
        body.addProperty("active", true);
        if (macAddress != null) body.addProperty("macAddress", macAddress);
        String response = post("/devices", gson.toJson(body));
        return response == null ? null : gson.fromJson(response, Device.class);
    }

    public List<Visitor> getVisitors() {
        String response = get("/visitors");
        if (response == null || response.isBlank()) return Collections.emptyList();
        Visitor[] visitors = gson.fromJson(response, Visitor[].class);
        return visitors == null ? Collections.emptyList() : Arrays.asList(visitors);
    }

    public Visitor createVisitor(String visitorName, String visitorType, String blockName, String apartmentNo, String visitReason) {
        JsonObject body = new JsonObject();
        body.addProperty("visitorName", visitorName);
        body.addProperty("visitorType", visitorType);
        body.addProperty("blockName", blockName);
        body.addProperty("apartmentNo", apartmentNo);
        body.addProperty("visitReason", visitReason);
        String response = post("/visitors", gson.toJson(body));
        return response == null ? null : gson.fromJson(response, Visitor.class);
    }

    public Visitor approveVisitor(Long id) { return updateVisitorStatus(id, "approve"); }
    public Visitor rejectVisitor(Long id) { return updateVisitorStatus(id, "reject"); }
    public Visitor exitVisitor(Long id) { return updateVisitorStatus(id, "exit"); }

    private Visitor updateVisitorStatus(Long id, String action) {
        if (id == null) return null;
        String response = put("/visitors/" + id + "/" + action);
        return response == null ? null : gson.fromJson(response, Visitor.class);
    }

    public List<Resident> getResidents() {
        String response = get("/residents");
        if (response == null || response.isBlank()) return Collections.emptyList();
        Resident[] residents = gson.fromJson(response, Resident[].class);
        return residents == null ? Collections.emptyList() : Arrays.asList(residents);
    }

    public Resident createResident(String fullName, String phone, String rfidId, Long apartmentId) {
        JsonObject body = new JsonObject();
        body.addProperty("fullName", fullName);
        body.addProperty("phone", phone);
        body.addProperty("rfidId", rfidId);
        if (apartmentId != null) body.addProperty("apartmentId", apartmentId);
        String response = post("/residents", gson.toJson(body));
        return response == null ? null : gson.fromJson(response, Resident.class);
    }

}