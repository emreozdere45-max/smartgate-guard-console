package com.smartgate.llm;

import com.smartgate.database.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class TextToSqlService {
    private final OllamaClient ollamaClient = new OllamaClient();

    private static final String SCHEMA_CONTEXT = """
        Veritabani semasi (PostgreSQL):
        - apartments (id, block_name, apartment_no, created_at)
        - residents (id, apartment_id, full_name, phone, rfid_id, is_active, created_at)
        - gate_logs (id, event_time TIMESTAMP, method VARCHAR, door_id VARCHAR, resident_id, note TEXT, device_id)
        - alarms (id, alarm_time TIMESTAMP, apartment_id, alarm_type VARCHAR, source_label VARCHAR, severity VARCHAR, is_resolved BOOLEAN, resolved_at TIMESTAMP)
        - chat_messages (id, apartment_id, sender_type VARCHAR, message_text TEXT, sent_at TIMESTAMP, delivery_status VARCHAR)
        - visitors (id, visitor_name VARCHAR, visitor_type VARCHAR, block_name VARCHAR, apartment_no, visit_reason TEXT, status VARCHAR, entry_time TIMESTAMP, exit_time TIMESTAMP, created_at TIMESTAMP)
        - intercom_devices (id, name, ip_address, command_port, location, is_active, created_at, updated_at)

        Kurallar:
        - Sadece SELECT sorgusu uret, baska hicbir sey yazma.
        - Turkce soru gelecek, PostgreSQL sorgusu dondur.
        - Sadece SQL yaz, aciklama ekleme, markdown kullanma.
        - Bugun icin CURRENT_DATE kullan.
        - Kapi acilma/giris loglari sorulursa gate_logs tablosunu kullan.
        - Kapi, cihaz, interkom paneli, kapi sayisi, kac kapi var, kac cihaz var gibi sorularda intercom_devices tablosunu kullan.
        - "Kac kapi var?" sorusu aktif kapi sayisi anlamina gelir.
        - Aktif kapi sayisi icin intercom_devices.is_active = true sartini kullan.
        - Kapi listesi istenirse name, ip_address, command_port, location kolonlarini dondur.
        - Kim geldi, kim girdi, kim cikti, ziyaretci, misafir, kurye, bakici, teknik servis gibi sorularda visitors tablosunu kullan.
        - Ziyaretci giris zamani icin entry_time, cikis zamani icin exit_time kullan.
        - Ziyaretci durumlari: PENDING, APPROVED, REJECTED, EXITED.
        - Daire bilgisi visitors tablosunda block_name ve apartment_no kolonlarindadir.
        - Bugunku ziyaretciler icin: entry_time >= CURRENT_DATE AND entry_time < CURRENT_DATE + INTERVAL '1 day'
        - Bugunku kapi loglari icin: event_time >= CURRENT_DATE AND event_time < CURRENT_DATE + INTERVAL '1 day'
        - Alarm sorularinda kullanici "tum", "toplam", "gecmis" veya "cozulen" demediyse sadece aktif alarmlari listele/say.
        - Aktif alarm demek: alarms.is_resolved = false.
        - Cozulen alarm demek: alarms.is_resolved = true.

        Ornekler:
        Soru: Kac kapi var?
        SQL: SELECT COUNT(*) AS aktif_kapi_sayisi FROM intercom_devices WHERE is_active = true

        Soru: Kac cihaz var?
        SQL: SELECT COUNT(*) AS aktif_cihaz_sayisi FROM intercom_devices WHERE is_active = true

        Soru: Kapilari listele
        SQL: SELECT name, ip_address, command_port, location FROM intercom_devices WHERE is_active = true ORDER BY name

        Soru: Tum kapilari goster
        SQL: SELECT name, ip_address, command_port, location, is_active FROM intercom_devices ORDER BY name

        Soru: Bugun kimler giris yapti?
        SQL: SELECT visitor_name, visitor_type, block_name, apartment_no, status, entry_time, exit_time FROM visitors WHERE entry_time >= CURRENT_DATE AND entry_time < CURRENT_DATE + INTERVAL '1 day' ORDER BY entry_time DESC

        Soru: Cikis yapan ziyaretcileri listele
        SQL: SELECT visitor_name, visitor_type, block_name, apartment_no, entry_time, exit_time FROM visitors WHERE status = 'EXITED' ORDER BY exit_time DESC

        Soru: Bekleyen ziyaretciler kimler?
        SQL: SELECT visitor_name, visitor_type, block_name, apartment_no, visit_reason, entry_time FROM visitors WHERE status = 'PENDING' ORDER BY entry_time DESC

        Soru: Bugun toplam kac ziyaretci geldi?
        SQL: SELECT COUNT(*) AS toplam_ziyaretci FROM visitors WHERE entry_time >= CURRENT_DATE AND entry_time < CURRENT_DATE + INTERVAL '1 day'

        Soru: Bugun kac kez kapi acildi?
        SQL: SELECT COUNT(*) AS kapi_acilma_sayisi FROM gate_logs WHERE event_time >= CURRENT_DATE AND event_time < CURRENT_DATE + INTERVAL '1 day'

        Soru: Aktif alarmlari listele
        SQL: SELECT alarm_time, alarm_type, severity, apartment_id, source_label FROM alarms WHERE is_resolved = false ORDER BY alarm_time DESC

        Soru: Kac alarm var?
        SQL: SELECT COUNT(*) AS aktif_alarm_sayisi FROM alarms WHERE is_resolved = false

        Soru: Cozulen alarmlari listele
        SQL: SELECT alarm_time, alarm_type, severity, apartment_id, source_label, resolved_at FROM alarms WHERE is_resolved = true ORDER BY resolved_at DESC
        """;

    public String generateSql(String userQuestion) {
        String deterministicSql = tryBuildDeterministicSql(userQuestion);
        if (!deterministicSql.isEmpty()) {
            return deterministicSql;
        }

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

    private String tryBuildDeterministicSql(String userQuestion) {
        String question = normalizeQuestion(userQuestion);

        String doorSql = tryBuildDoorDeviceSql(question);
        if (!doorSql.isEmpty()) {
            return doorSql;
        }

        String alarmSql = tryBuildAlarmSql(question);
        if (!alarmSql.isEmpty()) {
            return alarmSql;
        }

        return "";
    }

    private String tryBuildDoorDeviceSql(String question) {
        boolean mentionsDevice = question.contains("kapi") ||
                question.contains("cihaz") ||
                question.contains("interkom") ||
                question.contains("panel");

        if (!mentionsDevice) {
            return "";
        }

        boolean asksDoorLog = question.contains("acildi") ||
                question.contains("acilma") ||
                question.contains("giris") ||
                question.contains("log") ||
                question.contains("kayit");

        if (asksDoorLog) {
            return "";
        }

        boolean asksCount = question.contains("kac") ||
                question.contains("sayisi") ||
                question.contains("adet");

        boolean asksAll = question.contains("tum") ||
                question.contains("butun") ||
                question.contains("hepsi");

        boolean asksPassive = question.contains("pasif") ||
                question.contains("kapali");

        if (asksPassive && asksCount) {
            return "SELECT COUNT(*) AS pasif_kapi_sayisi FROM intercom_devices WHERE is_active = false";
        }

        if (asksPassive) {
            return "SELECT name, ip_address, command_port, location FROM intercom_devices WHERE is_active = false ORDER BY name";
        }

        if (asksAll && asksCount) {
            return "SELECT COUNT(*) AS toplam_kapi_sayisi FROM intercom_devices";
        }

        if (asksAll) {
            return "SELECT name, ip_address, command_port, location, is_active FROM intercom_devices ORDER BY name";
        }

        if (asksCount) {
            return "SELECT COUNT(*) AS aktif_kapi_sayisi FROM intercom_devices WHERE is_active = true";
        }

        return "SELECT name, ip_address, command_port, location FROM intercom_devices WHERE is_active = true ORDER BY name";
    }

    private String tryBuildAlarmSql(String question) {
        if (!question.contains("alarm")) {
            return "";
        }

        boolean asksCount = question.contains("kac") ||
                question.contains("sayisi") ||
                question.contains("adet");
        boolean asksResolved = question.contains("cozulen") ||
                question.contains("cozuldu") ||
                question.contains("kapali");
        boolean asksAllHistory = question.contains("tum") ||
                question.contains("butun") ||
                question.contains("toplam kayit") ||
                question.contains("gecmis");

        if (asksResolved && asksCount) {
            return "SELECT COUNT(*) AS cozulen_alarm_sayisi FROM alarms WHERE is_resolved = true";
        }

        if (asksResolved) {
            return "SELECT alarm_time, alarm_type, severity, apartment_id, source_label, resolved_at FROM alarms WHERE is_resolved = true ORDER BY resolved_at DESC";
        }

        if (asksAllHistory && asksCount) {
            return "SELECT COUNT(*) AS toplam_alarm_kaydi FROM alarms";
        }

        if (asksAllHistory) {
            return "SELECT alarm_time, alarm_type, severity, apartment_id, source_label, is_resolved, resolved_at FROM alarms ORDER BY alarm_time DESC";
        }

        if (asksCount) {
            return "SELECT COUNT(*) AS aktif_alarm_sayisi FROM alarms WHERE is_resolved = false";
        }

        return "SELECT alarm_time, alarm_type, severity, apartment_id, source_label FROM alarms WHERE is_resolved = false ORDER BY alarm_time DESC";
    }

    private String normalizeQuestion(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.forLanguageTag("tr-TR"))
                .replace("ı", "i")
                .replace("İ", "i")
                .replace("ğ", "g")
                .replace("ü", "u")
                .replace("ş", "s")
                .replace("ö", "o")
                .replace("ç", "c")
                .replace("Ä±", "i")
                .replace("Ä°", "i")
                .replace("ÄŸ", "g")
                .replace("Ã¼", "u")
                .replace("ÅŸ", "s")
                .replace("Ã¶", "o")
                .replace("Ã§", "c");
    }
}