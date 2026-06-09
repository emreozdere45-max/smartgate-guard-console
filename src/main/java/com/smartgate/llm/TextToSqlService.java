package com.smartgate.llm;

public class TextToSqlService {

    private final OllamaClient ollamaClient = new OllamaClient();

    private static final String SCHEMA_CONTEXT = """
            Veritabanı şeması:
            - residents (id, block_no, apartment_no, full_name, phone, rfid_id)
            - gate_logs (id, unlock_time, unlock_method, gate_id, resident_id)
            - alarms (id, alarm_time, apartment_no, alarm_type, resolved)
            - chat_messages (id, sent_at, sender_type, apartment_no, message)
            
            Kurallar:
            - Sadece SELECT sorgusu üret, başka hiçbir şey yazma.
            - Türkçe soru gelecek, SQL sorgusu döndür.
            - Sadece SQL yaz, açıklama ekleme, markdown kullanma.
            """;

    public String generateSql(String userQuestion) {
        String prompt = SCHEMA_CONTEXT + "\nSoru: " + userQuestion + "\nSQL:";
        String rawResponse = ollamaClient.chat(prompt);
        return cleanSql(rawResponse);
    }

    private String cleanSql(String raw) {
        return raw
                .replaceAll("```sql", "")
                .replaceAll("```", "")
                .replaceAll("(?i)sql:", "")
                .trim();
    }
}