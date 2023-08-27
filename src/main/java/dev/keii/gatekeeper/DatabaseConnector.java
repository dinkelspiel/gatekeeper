package dev.keii.gatekeeper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {
    public static void InitializeDatabase()
    {
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();

            statement.execute("CREATE TABLE IF NOT EXISTS `invites` (\n" +
                    "  `user_id` bigint(20) UNSIGNED NOT NULL,\n" +
                    "  `invite_uuid` varchar(36) NOT NULL,\n" +
                    "  `used` tinyint(1) NOT NULL DEFAULT 0,\n" +
                    "  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n");

            statement.execute("CREATE TABLE IF NOT EXISTS `users` (\n" +
                    "  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT,\n" +
                    "  `uuid` varchar(36) NOT NULL,\n" +
                    "  `recommended_by` bigint(20) UNSIGNED DEFAULT NULL,\n" +
                    "  `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;\n" +
                    "\n");

            statement.close();
            connection.close();
        } catch(SQLException e)
        {
            Bukkit.broadcast(Component.text("Failed initializing database!").color(NamedTextColor.RED));
        }
    }

    static String DB_URL = null;
    static String DB_NAME = null;
    static String DB_USER = null;
    static String DB_PASSWORD = null;

    public static Connection getConnection() throws SQLException {
        final String url = DB_URL + DB_NAME;

        return DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
    }
}