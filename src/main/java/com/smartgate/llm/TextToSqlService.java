package com.smartgate.llm;

import com.smartgate.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TextToSqlService {
    private final OllamaClient ollamaClient = new OllamaClient();

    private static final String SCHEMA_CONTEXT = """
        Veritabanı şeması (PostgreSQL):
        - residents (id, block_no, apartment_no, full_name, phone, rfid_id)
        - gate_logs (id, event_time TIMESTAMP, method VARCHAR, door_id VARCHAR, resident_id)
        - alarms (id, alarm_time TIMESTAMP, apartment_id, alarm_type VARCHAR, source_label VARCHAR, is_resolved BOOLEAN, resolved_at TIMESTAMP)
        - chat_messages (id, sent_at TIMESTAMP, sender_type VARCHAR, apartment_no VARCHAR, message TEXT)
        
        Kurallar:
        - Sadece SELECT sorgusu üret, başka hiçbir şey yazma.
        - Türkçe soru gelecek, SQL sorgusu döndür.
        - Sadece SQL yaz, açıklama ekleme, markdown kullanma.
        - PostgreSQL syntax kullan. Tarih için DATE('now') değil CURRENT_DATE kullan.
        - Bugün için: event_time >= CURRENT_DATE AND event_time < CURRENT_DATE + INTERVAL '1 day'
        """;

    public String generateSql(String userQuestion) {
        String prompt = SCHEMA_CONTEXT + "\nSoru: " + userQuestion + "\nSQL:";
        String rawResponse = ollamaClient.chat(prompt);
        return cleanSql(rawResponse);
    }

    public String generateAndExecute(String userQuestion) {
        String sql = generateSql(userQuestion);
        if (sql.isEmpty()) return "SQL üretilemedi.";

        StringBuilder result = new StringBuilder();
        result.append("SQL: ").append(sql).append("\n\nSonuçlar:\n");

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // Kolon başlıkları
            for (int i = 1; i <= colCount; i++) {
                result.append(meta.getColumnName(i));
                if (i < colCount) result.append(" | ");
            }
            result.append("\n").append("-".repeat(60)).append("\n");

            // Satırlar
            int rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    result.append(rs.getString(i));
                    if (i < colCount) result.append(" | ");
                }
                result.append("\n");
                rowCount++;
            }

            if (rowCount == 0) result.append("Sonuç bulunamadı.");

        } catch (SQLException e) {
            result.append("Sorgu hatası: ").append(e.getMessage());
        }

        return result.toString();
    }

    private String cleanSql(String raw) {
        return raw
                .replaceAll("```sql", "")
                .replaceAll("```", "")
                .replaceAll("(?i)sql:", "")
                .trim();
    }
}