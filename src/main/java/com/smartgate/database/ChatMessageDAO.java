package com.smartgate.database;

import com.smartgate.model.ChatMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageDAO {

    public void insert(ChatMessage message) {
        String sql = "INSERT INTO chat_messages (apartment_id, device_id, sender_type, message_text, sent_at, delivery_status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (message.getApartmentId() == null) {
                stmt.setNull(1, Types.BIGINT);
            } else {
                stmt.setLong(1, message.getApartmentId());
            }
            if (message.getDeviceId() == null) {
                stmt.setNull(2, Types.BIGINT);
            } else {
                stmt.setLong(2, message.getDeviceId());
            }
            stmt.setString(3, message.getSenderType());
            stmt.setString(4, message.getMessageText());
            stmt.setTimestamp(5, Timestamp.valueOf(message.getSentAt()));
            stmt.setString(6, "sent");
            stmt.executeUpdate();
            System.out.println("Mesaj kaydedildi.");
        } catch (SQLException e) {
            System.err.println("ChatMessage insert hatası: " + e.getMessage());
        }
    }

    public List<ChatMessage> getAll() {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT * FROM chat_messages ORDER BY sent_at DESC LIMIT 100";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ChatMessage m = new ChatMessage();
                m.setId(rs.getLong("id"));
                m.setApartmentId(rs.getLong("apartment_id"));
                m.setSenderType(rs.getString("sender_type"));
                m.setMessageText(rs.getString("message_text"));
                m.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                m.setDeliveryStatus(rs.getString("delivery_status"));
                messages.add(m);
            }
        } catch (SQLException e) {
            System.err.println("ChatMessage getAll hatası: " + e.getMessage());
        }
        return messages;
    }

    public List<ChatMessage> getByApartmentId(Long apartmentId) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT * FROM chat_messages WHERE apartment_id = ? ORDER BY sent_at ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, apartmentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChatMessage m = new ChatMessage();
                m.setId(rs.getLong("id"));
                m.setApartmentId(rs.getLong("apartment_id"));
                m.setSenderType(rs.getString("sender_type"));
                m.setMessageText(rs.getString("message_text"));
                m.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                m.setDeliveryStatus(rs.getString("delivery_status"));
                messages.add(m);
            }
        } catch (SQLException e) {
            System.err.println("ChatMessage getByApartment hatası: " + e.getMessage());
        }
        return messages;
    }
    public List<ChatMessage> getByDeviceId(Long deviceId) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT * FROM chat_messages WHERE device_id = ? ORDER BY sent_at ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, deviceId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChatMessage m = new ChatMessage();
                m.setId(rs.getLong("id"));
                m.setDeviceId(rs.getLong("device_id"));
                m.setSenderType(rs.getString("sender_type"));
                m.setMessageText(rs.getString("message_text"));
                m.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                m.setDeliveryStatus(rs.getString("delivery_status"));
                messages.add(m);
            }
        } catch (SQLException e) {
            System.err.println("ChatMessage getByDeviceId hatası: " + e.getMessage());
        }
        return messages;
    }

}