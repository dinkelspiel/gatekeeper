package dev.keii.gatekeeper;

import dev.keii.gatekeeper.commands.CommandInvite;
import dev.keii.gatekeeper.events.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Gatekeeper extends JavaPlugin {

    private static Gatekeeper instance;
    public static Config config;

    @Override
    public void onEnable() {
        instance = this;

        config = new Config();
        config.loadConfig();

        registerEvents();
        registerCommands();

        DatabaseConnector.InitializeDatabase();

        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            PlayerJoin.loginUser(player, false);
        }
    }

    public void registerEvents() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new PlayerJoin(), this);
        pm.registerEvents(new PlayerQuit(), this);
    }

    public void registerCommands() {
        this.getCommand("invite").setExecutor(new CommandInvite());
    }

    public static Gatekeeper getInstance()
    {
        return instance;
    }
}
