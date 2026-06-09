package com.smartgate.database;

import com.smartgate.model.Resident;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResidentDAO {

    public void insert(Resident resident) {
        String sql = "INSERT INTO residents (block_no, apartment_no, full_name, phone, rfid_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, resident.getBlockNo());
            stmt.setString(2, resident.getApartmentNo());
            stmt.setString(3, resident.getFullName());
            stmt.setString(4, resident.getPhone());
            stmt.setString(5, resident.getRfidId());
            stmt.executeUpdate();
            System.out.println("Resident kaydedildi.");
        } catch (SQLException e) {
            System.err.println("Resident insert hatası: " + e.getMessage());
        }
    }

    public List<Resident> getAll() {
        List<Resident> residents = new ArrayList<>();
        String sql = "SELECT * FROM residents ORDER BY full_name";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Resident r = new Resident();
                r.setId(rs.getInt("id"));
                r.setBlockNo(rs.getString("block_no"));
                r.setApartmentNo(rs.getString("apartment_no"));
                r.setFullName(rs.getString("full_name"));
                r.setPhone(rs.getString("phone"));
                r.setRfidId(rs.getString("rfid_id"));
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
                r.setId(rs.getInt("id"));
                r.setBlockNo(rs.getString("block_no"));
                r.setApartmentNo(rs.getString("apartment_no"));
                r.setFullName(rs.getString("full_name"));
                r.setPhone(rs.getString("phone"));
                r.setRfidId(rs.getString("rfid_id"));
                return r;
            }
        } catch (SQLException e) {
            System.err.println("Resident findByRfid hatası: " + e.getMessage());
        }
        return null;
    }
}