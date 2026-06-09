package com.smartgate.database;

import com.smartgate.model.Alarm;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlarmDAO {

    public void insert(Alarm alarm) {
        String sql = "INSERT INTO alarms (alarm_time, alarm_type, source_label, is_resolved) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(alarm.getAlarmTime()));
            stmt.setString(2, alarm.getAlarmType());
            stmt.setString(3, alarm.getApartmentNo());
            stmt.setBoolean(4, alarm.isResolved());
            stmt.executeUpdate();
            System.out.println("Alarm kaydedildi.");
        } catch (SQLException e) {
            System.err.println("Alarm insert hatası: " + e.getMessage());
        }
    }

    public List<Alarm> getUnresolved() {
        List<Alarm> alarms = new ArrayList<>();
        String sql = "SELECT * FROM alarms WHERE is_resolved = false ORDER BY alarm_time DESC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Alarm a = new Alarm();
                a.setId(rs.getInt("id"));
                a.setAlarmTime(rs.getTimestamp("alarm_time").toLocalDateTime());
                a.setAlarmType(rs.getString("alarm_type"));
                a.setApartmentNo(rs.getString("source_label"));
                a.setResolved(rs.getBoolean("is_resolved"));
                alarms.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Alarm getUnresolved hatası: " + e.getMessage());
        }
        return alarms;
    }

    public void markResolved(int alarmId) {
        String sql = "UPDATE alarms SET is_resolved = true, resolved_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, alarmId);
            stmt.executeUpdate();
            System.out.println("Alarm çözüldü: " + alarmId);
        } catch (SQLException e) {
            System.err.println("Alarm markResolved hatası: " + e.getMessage());
        }
    }
}