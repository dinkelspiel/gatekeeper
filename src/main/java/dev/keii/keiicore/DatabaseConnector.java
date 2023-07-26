package dev.keii.keiicore;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    public static Connection getConnection() throws SQLException {

        Dotenv dotenv = Dotenv.load();

        final String DB_URL = "jdbc:mysql://localhost:3306/" + dotenv.get("DB_NAME");
        final String USER = dotenv.get("DB_USER");
        final String PASSWORD = dotenv.get("DB_PASSWORD");


        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
}