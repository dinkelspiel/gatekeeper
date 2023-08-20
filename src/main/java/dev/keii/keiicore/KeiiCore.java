package dev.keii.keiicore;

import dev.keii.keiicore.commands.CommandConfig;
import dev.keii.keiicore.commands.CommandInvite;
import dev.keii.keiicore.commands.CommandMap;
import dev.keii.keiicore.commands.CommandSession;
import dev.keii.keiicore.events.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class KeiiCore extends JavaPlugin {

    private static KeiiCore instance;
    public static List<RuntimeError> RuntimeErrors = new ArrayList<>();
    public static HashMap<String, String> Sessions = new HashMap<>();

    public static Config config;

    @Override
    public void onEnable() {
        instance = this;

        config = new Config();
        config.loadConfig();

        registerEvents();
        registerCommands();

        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            PlayerJoin.loginUser(player, false);
        }
    }

    public void registerEvents() {
        //This first line is optional, makes it faster with lots of classes
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new PlayerJoin(), this);
        pm.registerEvents(new PlayerQuit(), this);
        pm.registerEvents(new BlockBreak(), this);
        pm.registerEvents(new BlockPlace(), this);
        pm.registerEvents(new BucketEmpty(), this);
        pm.registerEvents(new BucketFill(), this);
        pm.registerEvents(new PlayerInteract(), this);
        pm.registerEvents(new InventoryClick(), this);
        pm.registerEvents(new PlayerMove(), this);
        pm.registerEvents(new EntityExplode(), this);
        pm.registerEvents(new PlayerChat(), this);
        pm.registerEvents(new PlayerResourcePack(), this);
    }

    public void registerCommands() {
        this.getCommand("invite").setExecutor(new CommandInvite());
        this.getCommand("session").setExecutor(new CommandSession());
        this.getCommand("map").setExecutor(new CommandMap());
        this.getCommand("config").setExecutor(new CommandConfig());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static KeiiCore getInstance()
    {
        return instance;
    }
}
