package dev.keii.keiicore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    public boolean Claims;
    public boolean Invite;

    public String ApiUrl;

    public String DbUrl;
    public String DbName;
    public String DbUser;
    public String DbPassword;


    public void fileConfigToConfig(FileConfiguration config)
    {
        Claims = config.getBoolean("claims");
        Invite = config.getBoolean("invite");

        Bukkit.broadcastMessage(String.valueOf(config.getBoolean("invite")));

        DbUrl = config.getString("dbUrl");
        DbName = config.getString("dbName");
        DbUser = config.getString("dbUser");
        DbPassword = config.getString("dbPassword");

        ApiUrl = config.getString("apiUrl");
    }

    public void loadConfig() {
        FileConfiguration config = KeiiCore.getInstance().getConfig();

        if(config.getString("claims") == null)
            config.set("claims", true);
        if(config.getString("invite") == null)
            config.set("invite", true);

        if(config.getString("dbUrl") == null)
            config.set("dbUrl", "jdbc:mysql://localhost:3306/");
        if(config.getString("dbName") == null)
            config.set("dbName", "");
        if(config.getString("dbUser") == null)
            config.set("dbUser", "");
        if(config.getString("dbPassword") == null)
            config.set("dbPassword", "");

        if(config.getString("apiUrl") == null)
            config.set("apiUrl", "https://example.com");

        config.options().copyDefaults(true);
        KeiiCore.getInstance().saveConfig();

        config = KeiiCore.getInstance().getConfig();

        if(config.getString("dbName").isEmpty())
        {
            Bukkit.broadcast(Component.text("Database is not set in the config. Core functionality of the plugin ").color(NamedTextColor.RED)
                    .append(
                            Component.text("will not")
                                    .decorate(TextDecoration.ITALIC)
                                    .decorate(TextDecoration.BOLD))
                    .color(NamedTextColor.RED)
                    .append(
                            Component.text(" work")
                                    .color(NamedTextColor.RED)));
        }

        fileConfigToConfig(config);

        DatabaseConnector.DB_URL = DbUrl;
        DatabaseConnector.DB_NAME = DbName;
        DatabaseConnector.DB_USER = DbUser;
        DatabaseConnector.DB_PASSWORD = DbPassword;
    }
}
