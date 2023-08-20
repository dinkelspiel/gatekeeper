package dev.keii.keiicore.commands;

import dev.keii.keiicore.Config;
import dev.keii.keiicore.KeiiCore;
import dev.keii.keiicore.inventories.InventoryMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandConfig implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(args.length == 0)
        {
            sender.sendMessage(Component.text("No arguments provided").color(NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "load" -> {
                sender.sendMessage(Component.text("Load has problems. Use /reload to reload config.").color(NamedTextColor.RED));
                return true;

//                KeiiCore.config = new Config();
//                KeiiCore.config.loadConfig();
//                sender.sendMessage(Component.text("Loaded config successfully.").color(NamedTextColor.YELLOW));
            }
            case "get" -> {
                if (args.length == 1) {
                    sender.sendMessage(Component.text("/config get <config key>").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("Config Keys: Claims, Invite").color(NamedTextColor.RED));
                    sender.sendMessage(Component.text("Database login is omitted for security reasons").color(NamedTextColor.RED));
                    return true;
                }
                Object value;
                switch (args[1].toLowerCase()) {
                    case "claims" -> value = KeiiCore.config.Claims;
                    case "invite" -> value = KeiiCore.config.Invite;
                    default -> {
                        sender.sendMessage(Component.text("Config key doesn't exist").color(NamedTextColor.RED));
                        return true;
                    }
                }
                sender.sendMessage(Component.text(String.valueOf(value)).color(NamedTextColor.YELLOW));
                return true;
            }
            default -> {
                sender.sendMessage(Component.text("/config").color(NamedTextColor.RED));
                sender.sendMessage(Component.text("- load                  Loads the config").color(NamedTextColor.RED));
                sender.sendMessage(Component.text("- get <config key>      Gets a config specific key").color(NamedTextColor.RED));
                return true;
            }
        }
    }
}