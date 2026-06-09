package com.smartgate.database;
import com.smartgate.ConfigManager;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String HOST = ConfigManager.get("DB_HOST", "10.194.166.29");
    private static final String PORT = ConfigManager.get("DB_PORT", "5433");
    private static final String DB_NAME = "smartgate_db";
    private static final String USER = "smartgate_user";
    private static final String PASSWORD = "smartgate_password";

    private static final String URL = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME;

    private static Connection connection = null;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("PostgreSQL bağlantısı kuruldu.");
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("PostgreSQL bağlantısı kapatıldı.");
            }
        } catch (SQLException e) {
            System.err.println("Bağlantı kapatılırken hata: " + e.getMessage());
        }
    }
}