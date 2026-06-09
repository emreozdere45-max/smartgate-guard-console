package com.smartgate.database;

import com.smartgate.model.GateLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GateLogDAO {

    public void insert(GateLog log) {
        String sql = "INSERT INTO gate_logs (event_time, method, door_id, resident_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(log.getUnlockTime()));
            stmt.setString(2, log.getUnlockMethod());
            stmt.setString(3, String.valueOf(log.getGateId()));
            if (log.getResidentId() == 0) {
                stmt.setNull(4, java.sql.Types.BIGINT);
            } else {
                stmt.setInt(4, log.getResidentId());
            }
            stmt.executeUpdate();
            System.out.println("GateLog kaydedildi.");
        } catch (SQLException e) {
            System.err.println("GateLog insert hatası: " + e.getMessage());
        }
    }

    public List<GateLog> getAll() {
        List<GateLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM gate_logs ORDER BY event_time DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                GateLog log = new GateLog();
                log.setId(rs.getInt("id"));
                log.setUnlockTime(rs.getTimestamp("event_time").toLocalDateTime());
                log.setUnlockMethod(rs.getString("method"));
                log.setGateId(rs.getInt("resident_id"));
                log.setResidentId(rs.getInt("resident_id"));
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("GateLog getAll hatası: " + e.getMessage());
        }
        return logs;
    }
}