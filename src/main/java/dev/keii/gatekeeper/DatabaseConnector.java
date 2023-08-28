package dev.keii.gatekeeper;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {
    public static void initializeDatabase(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS invites (\n" +
                "    user_id integer PRIMARY KEY,\n" +
                "    invite_uuid text NOT NULL,\n" +
                "    used integer NOT NULL DEFAULT 0\n" +
        ");";

        Statement statement = connection.createStatement();
        statement.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS users (\n" +
                "    id integer PRIMARY KEY,\n" +
                "    uuid text NOT NULL,\n" +
                "    recommended_by integer\n" +
              ");";

        statement.execute(sql);

        statement.close();
    }

    public static  Connection getConnection() throws SQLException {
        File f = new File("./plugins/Gatekeeper");
        f.mkdir();

        String url = "jdbc:sqlite:./plugins/Gatekeeper/database.db";
        // create a connection to the database
        return DriverManager.getConnection(url);
    }
}
