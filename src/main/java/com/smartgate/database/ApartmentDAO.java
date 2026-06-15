package com.smartgate.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApartmentDAO {

    public List<String> getAllApartments() {
        List<String> apartments = new ArrayList<>();
        String sql = "SELECT block_name, apartment_no FROM apartments ORDER BY block_name, apartment_no";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                apartments.add(rs.getString("block_name") + "-" + rs.getString("apartment_no"));
            }
        } catch (SQLException e) {
            System.err.println("ApartmentDAO hatası: " + e.getMessage());
        }
        return apartments;
    }

    public Long getApartmentId(String blockName, String apartmentNo) {
        String sql = "SELECT id FROM apartments WHERE block_name = ? AND apartment_no = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, blockName);
            stmt.setString(2, apartmentNo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong("id");
        } catch (SQLException e) {
            System.err.println("ApartmentDAO getId hatası: " + e.getMessage());
        }
        return null;
    }
}