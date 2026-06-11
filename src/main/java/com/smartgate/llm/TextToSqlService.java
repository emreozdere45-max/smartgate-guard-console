package com.smartgate.llm;

import com.smartgate.database.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class TextToSqlService {
    private final OllamaClient ollamaClient = new OllamaClient();

    private static final String SCHEMA_CONTEXT = """
        Veritabani semasi (PostgreSQL):
        - apartments (id, block_name, apartment_no, created_at)
        - residents (id, apartment_id, full_name, phone, rfid_id, is_active, created_at)
        - gate_logs (id, event_time TIMESTAMP, method VARCHAR, door_id VARCHAR, resident_id, note TEXT, device_id)
        - alarms (id, alarm_time TIMESTAMP, apartment_id, alarm_type VARCHAR, source_label VARCHAR, severity VARCHAR, is_resolved BOOLEAN, resolved_at TIMESTAMP)
        - chat_messages (id, apartment_id, sender_type VARCHAR, message_text TEXT, sent_at TIMESTAMP, delivery_status VARCHAR)
        - visitors (id, visitor_name VARCHAR, visitor_type VARCHAR, block_name VARCHAR, apartment_no VARCHAR, visit_reason TEXT, status VARCHAR, entry_time TIMESTAMP, exit_time TIMESTAMP, created_at TIMESTAMP)

        Kurallar:
        - Sadece SELECT sorgusu uret, baska hicbir sey yazma.
        - Turkce soru gelecek, PostgreSQL sorgusu dondur.
        - Sadece SQL yaz, aciklama ekleme, markdown kullanma.
        - Bugun icin CURRENT_DATE kullan.
        - Kapi acilma/giris loglari sorulursa gate_logs tablosunu kullan.
        - Kim geldi, kim girdi, kim cikti, ziyaretci, misafir, kurye, bakici, teknik servis gibi sorularda visitors tablosunu kullan.
        - Ziyaretci giris zamani icin entry_time, cikis zamani icin exit_time kullan.
        - Ziyaretci durumlari: PENDING, APPROVED, REJECTED, EXITED.
        - Daire bilgisi visitors tablosunda block_name ve apartment_no kolonlarindadir.
        - Bugunku ziyaretciler icin: entry_time >= CURRENT_DATE AND entry_time < CURRENT_DATE + INTERVAL '1 day'
        - Bugunku kapi loglari icin: event_time >= CURRENT_DATE AND event_time < CURRENT_DATE + INTERVAL '1 day'

        Ornekler:
        Soru: Bugun kimler giris yapti?
        SQL: SELECT visitor_name, visitor_type, block_name, apartment_no, status, entry_time, exit_time FROM visitors WHERE entry_time >= CURRENT_DATE AND entry_time < CURRENT_DATE + INTERVAL '1 day' ORDER BY entry_time DESC

        Soru: Cikis yapan ziyaretcileri listele
        SQL: SELECT visitor_name, visitor_type, block_name, apartment_no, entry_time, exit_time FROM visitors WHERE status = 'EXITED' ORDER BY exit_time DESC

        Soru: Bekleyen ziyaretciler kimler?
        SQL: SELECT visitor_name, visitor_type, block_name, apartment_no, visit_reason, entry_time FROM visitors WHERE status = 'PENDING' ORDER BY entry_time DESC

        Soru: Bugun kimler cikis yapti?
        SQL: SELECT visitor_name, visitor_type, block_name, apartment_no, entry_time, exit_time FROM visitors WHERE exit_time >= CURRENT_DATE AND exit_time < CURRENT_DATE + INTERVAL '1 day' ORDER BY exit_time DESC

        Soru: Onaylanan ziyaretcileri listele
        SQL: SELECT visitor_name, visitor_type, block_name, apartment_no, visit_reason, entry_time FROM visitors WHERE status = 'APPROVED' ORDER BY entry_time DESC

        Soru: Reddedilen ziyaretcileri listele
        SQL: SELECT visitor_name, visitor_type, block_name, apartment_no, visit_reason, entry_time FROM visitors WHERE status = 'REJECTED' ORDER BY entry_time DESC

        Soru: Bugun gelen kuryeleri goster
        SQL: SELECT visitor_name, block_name, apartment_no, visit_reason, status, entry_time FROM visitors WHERE visitor_type = 'KURYE' AND entry_time >= CURRENT_DATE AND entry_time < CURRENT_DATE + INTERVAL '1 day' ORDER BY entry_time DESC

        Soru: Bugun gelen misafirleri goster
        SQL: SELECT visitor_name, block_name, apartment_no, visit_reason, status, entry_time FROM visitors WHERE visitor_type = 'MISAFIR' AND entry_time >= CURRENT_DATE AND entry_time < CURRENT_DATE + INTERVAL '1 day' ORDER BY entry_time DESC

        Soru: A blok 12 dairesine gelenleri goster
        SQL: SELECT visitor_name, visitor_type, visit_reason, status, entry_time, exit_time FROM visitors WHERE LOWER(block_name) = 'a' AND apartment_no = '12' ORDER BY entry_time DESC

        Soru: En son gelen 10 ziyaretciyi listele
        SQL: SELECT visitor_name, visitor_type, block_name, apartment_no, status, entry_time FROM visitors ORDER BY entry_time DESC LIMIT 10

        Soru: Cikis yapmamis ziyaretciler kimler?
        SQL: SELECT visitor_name, visitor_type, block_name, apartment_no, status, entry_time FROM visitors WHERE exit_time IS NULL ORDER BY entry_time DESC

        Soru: Bugun toplam kac ziyaretci geldi?
        SQL: SELECT COUNT(*) AS toplam_ziyaretci FROM visitors WHERE entry_time >= CURRENT_DATE AND entry_time < CURRENT_DATE + INTERVAL '1 day'

        Soru: Bugun kac kurye geldi?
        SQL: SELECT COUNT(*) AS kurye_sayisi FROM visitors WHERE visitor_type = 'KURYE' AND entry_time >= CURRENT_DATE AND entry_time < CURRENT_DATE + INTERVAL '1 day'

        Soru: Bugun kac misafir geldi?
        SQL: SELECT COUNT(*) AS misafir_sayisi FROM visitors WHERE visitor_type = 'MISAFIR' AND entry_time >= CURRENT_DATE AND entry_time < CURRENT_DATE + INTERVAL '1 day'

        Soru: Kac kisi cikis yapti?
        SQL: SELECT COUNT(*) AS cikis_yapan_sayisi FROM visitors WHERE status = 'EXITED'

        Soru: Kac kisi hala iceride gorunuyor?
        SQL: SELECT COUNT(*) AS iceride_gorunen_sayisi FROM visitors WHERE exit_time IS NULL AND status IN ('PENDING', 'APPROVED')

        Soru: Hangi daireye en cok ziyaretci geldi?
        SQL: SELECT block_name, apartment_no, COUNT(*) AS ziyaretci_sayisi FROM visitors GROUP BY block_name, apartment_no ORDER BY ziyaretci_sayisi DESC

        Soru: Bugun en cok ziyaretci alan daireler
        SQL: SELECT block_name, apartment_no, COUNT(*) AS ziyaretci_sayisi FROM visitors WHERE entry_time >= CURRENT_DATE AND entry_time < CURRENT_DATE + INTERVAL '1 day' GROUP BY block_name, apartment_no ORDER BY ziyaretci_sayisi DESC

        Soru: Ziyaretci sebebi yemek teslimati olanlari listele
        SQL: SELECT visitor_name, visitor_type, block_name, apartment_no, visit_reason, entry_time FROM visitors WHERE LOWER(visit_reason) LIKE '%yemek%' ORDER BY entry_time DESC

        Soru: Bugun kac kez kapi acildi?
        SQL: SELECT COUNT(*) AS kapi_acilma_sayisi FROM gate_logs WHERE event_time >= CURRENT_DATE AND event_time < CURRENT_DATE + INTERVAL '1 day'

        Soru: Son 10 kapi acilma kaydini goster
        SQL: SELECT event_time, method, door_id, resident_id, note FROM gate_logs ORDER BY event_time DESC LIMIT 10

        Soru: Kapi hangi yontemlerle acildi?
        SQL: SELECT method, COUNT(*) AS adet FROM gate_logs GROUP BY method ORDER BY adet DESC

        Soru: Bugunku kapi giris kayitlarini listele
        SQL: SELECT event_time, method, door_id, resident_id, note FROM gate_logs WHERE event_time >= CURRENT_DATE AND event_time < CURRENT_DATE + INTERVAL '1 day' ORDER BY event_time DESC

        Soru: En son kapi ne zaman acildi?
        SQL: SELECT event_time, method, door_id FROM gate_logs ORDER BY event_time DESC LIMIT 1

        Soru: Aktif alarmlari listele
        SQL: SELECT alarm_time, alarm_type, severity, apartment_id, source_label FROM alarms WHERE is_resolved = false ORDER BY alarm_time DESC

        Soru: Bugunku alarmlari goster
        SQL: SELECT alarm_time, alarm_type, severity, apartment_id, source_label, is_resolved FROM alarms WHERE alarm_time >= CURRENT_DATE AND alarm_time < CURRENT_DATE + INTERVAL '1 day' ORDER BY alarm_time DESC

        Soru: Cozulmemis alarmlar hangileri?
        SQL: SELECT alarm_time, alarm_type, severity, apartment_id, source_label FROM alarms WHERE is_resolved = false ORDER BY alarm_time DESC

        Soru: Yangin alarmlarini listele
        SQL: SELECT alarm_time, severity, apartment_id, source_label, is_resolved FROM alarms WHERE LOWER(alarm_type) LIKE '%yangin%' OR LOWER(alarm_type) LIKE '%fire%' ORDER BY alarm_time DESC

        Soru: Son 24 saatte gelen alarmlar
        SQL: SELECT alarm_time, alarm_type, severity, apartment_id, source_label, is_resolved FROM alarms WHERE alarm_time >= NOW() - INTERVAL '24 hours' ORDER BY alarm_time DESC
        """;

    public String generateSql(String userQuestion) {
        String prompt = SCHEMA_CONTEXT + "\nSoru: " + userQuestion + "\nSQL:";
        String rawResponse = ollamaClient.chat(prompt);
        return cleanSql(rawResponse);
    }

    public String generateAndExecute(String userQuestion) {
        String sql = generateSql(userQuestion);
        if (sql.isEmpty()) {
            return "SQL uretilemedi.";
        }
        if (!isSafeSelect(sql)) {
            return "Sadece SELECT sorgulari calistirilabilir.";
        }

        StringBuilder result = new StringBuilder();
        result.append("SQL: ").append(sql).append("\n\nSonuclar:\n");

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            for (int i = 1; i <= colCount; i++) {
                result.append(meta.getColumnName(i));
                if (i < colCount) {
                    result.append(" | ");
                }
            }
            result.append("\n").append("-".repeat(60)).append("\n");

            int rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    result.append(rs.getString(i));
                    if (i < colCount) {
                        result.append(" | ");
                    }
                }
                result.append("\n");
                rowCount++;
            }

            if (rowCount == 0) {
                result.append("Sonuc bulunamadi.");
            }
        } catch (SQLException e) {
            result.append("Sorgu hatasi: ").append(e.getMessage());
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

    private boolean isSafeSelect(String sql) {
        String normalized = sql.trim().toLowerCase();
        return normalized.startsWith("select") && !normalized.contains(";");
    }
}
