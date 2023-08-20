package dev.keii.keiicore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    static String DB_URL = null;
    static String DB_NAME = null;
    static String DB_USER = null;
    static String DB_PASSWORD = null;

    public static Connection getConnection() throws SQLException {
        final String url = DB_URL + DB_NAME;

        return DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
    }
}