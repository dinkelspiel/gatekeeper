package dev.keii.gatekeeper.commands;

import dev.keii.gatekeeper.Gatekeeper;
import dev.keii.gatekeeper.Invite;
import dev.keii.gatekeeper.User;
import dev.keii.gatekeeper.events.PlayerJoin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.sql.StatementEvent;
import java.sql.*;


public class CommandGatekeeper implements CommandExecutor {
    private void sendInfo(CommandSender sender)
    {
        sender.sendMessage(Component.text("Keii's Gatekeeper").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Version: " + Gatekeeper.getInstance().getDescription().getVersion()).color(NamedTextColor.YELLOW));
    }

    private void sendHelp(CommandSender sender)
    {
        sender.sendMessage(
                Component.text("/invite <Minecraft Username>").color(NamedTextColor.GOLD)
                    .appendNewline()
                    .append(Component.text("    Invite a player").color(NamedTextColor.YELLOW))
                    .appendNewline()
                    .append(Component.text("/gatekeeper").color(NamedTextColor.GOLD))
                    .appendNewline()
                    .append(Component.text("    info - Get info about the plugin").color(NamedTextColor.YELLOW))
                    .appendNewline()
                    .append(Component.text("    save - Save the data (sqlite)").color(NamedTextColor.YELLOW))
                    .appendNewline()
                    .append(Component.text("    load - Load the data (sqlite)").color(NamedTextColor.YELLOW))
                    .appendNewline()
                    .append(Component.text("    help - Get help for gatekeeper").color(NamedTextColor.YELLOW))
        );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 1)
        {
            sendHelp(sender);
            return true;
        }

        switch(args[0])
        {
            case "info":
                sendInfo(sender);
                break;
            case "save":
                Gatekeeper.save(sender);
                break;
            case "load":
                Gatekeeper.load(sender);
                break;
            case "help":
                default:
                    sendHelp(sender);
        }

        return true;
    }
}