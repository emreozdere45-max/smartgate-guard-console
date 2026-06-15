package com.smartgate.database;

import com.smartgate.model.GateLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GateLogDAO {

    public void insert(GateLog log) {
        String sql = "INSERT INTO gate_logs (event_time, method, door_id, resident_id, device_id, note) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(log.getUnlockTime()));
            stmt.setString(2, log.getUnlockMethod());
            stmt.setString(3, log.getDoorId() != null ? log.getDoorId() : "main");
            if (log.getResidentId() == 0) {
                stmt.setNull(4, java.sql.Types.BIGINT);
            } else {
                stmt.setInt(4, log.getResidentId());
            }
            if (log.getDeviceId() == null) {
                stmt.setNull(5, java.sql.Types.BIGINT);
            } else {
                stmt.setLong(5, log.getDeviceId());
            }
            stmt.setString(6, log.getNote());
            stmt.executeUpdate();
            System.out.println("GateLog kaydedildi.");
        } catch (SQLException e) {
            System.err.println("GateLog insert hatası: " + e.getMessage());
        }
    }

    public List<GateLog> getAll() {
        List<GateLog> logs = new ArrayList<>();
        String sql = "SELECT g.*, d.name as device_name FROM gate_logs g " +
                "LEFT JOIN intercom_devices d ON g.device_id = d.id " +
                "ORDER BY g.event_time DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                GateLog log = new GateLog();
                log.setId(rs.getInt("id"));
                log.setUnlockTime(rs.getTimestamp("event_time").toLocalDateTime());
                log.setUnlockMethod(rs.getString("method"));
                log.setDoorId(rs.getString("door_id"));
                log.setResidentId(rs.getInt("resident_id"));
                log.setDeviceId(rs.getLong("device_id"));
                log.setNote(rs.getString("device_name"));
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("GateLog getAll hatası: " + e.getMessage());
        }
        return logs;
    }
    public List<GateLog> getByDeviceId(Long deviceId) {
        List<GateLog> logs = new ArrayList<>();
        String sql = "SELECT g.*, d.name as device_name FROM gate_logs g " +
                "LEFT JOIN intercom_devices d ON g.device_id = d.id " +
                "WHERE g.device_id = ? " +
                "ORDER BY g.event_time DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, deviceId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                GateLog log = new GateLog();
                log.setId(rs.getInt("id"));
                log.setUnlockTime(rs.getTimestamp("event_time").toLocalDateTime());
                log.setUnlockMethod(rs.getString("method"));
                log.setDoorId(rs.getString("door_id"));
                log.setDeviceId(rs.getLong("device_id"));
                log.setNote(rs.getString("device_name"));
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("GateLog getByDeviceId hatası: " + e.getMessage());
        }
        return logs;
    }
}