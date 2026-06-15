package com.smartgate.database;

import com.smartgate.model.Resident;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResidentDAO {

    public List<Resident> getAll() {
        List<Resident> residents = new ArrayList<>();
        String sql = "SELECT * FROM residents ORDER BY full_name";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Resident r = new Resident();
                r.setId(rs.getLong("id"));
                r.setFullName(rs.getString("full_name"));
                r.setPhone(rs.getString("phone"));
                r.setRfidId(rs.getString("rfid_id"));
                r.setApartmentId(rs.getLong("apartment_id"));
                r.setActive(rs.getBoolean("is_active"));
                residents.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Resident getAll hatası: " + e.getMessage());
        }
        return residents;
    }

    public Resident findByRfid(String rfidId) {
        String sql = "SELECT * FROM residents WHERE rfid_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rfidId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Resident r = new Resident();
                r.setId(rs.getLong("id"));
                r.setFullName(rs.getString("full_name"));
                r.setPhone(rs.getString("phone"));
                r.setRfidId(rs.getString("rfid_id"));
                r.setApartmentId(rs.getLong("apartment_id"));
                r.setActive(rs.getBoolean("is_active"));
                return r;
            }
        } catch (SQLException e) {
            System.err.println("Resident findByRfid hatası: " + e.getMessage());
        }
        return null;
    }
}