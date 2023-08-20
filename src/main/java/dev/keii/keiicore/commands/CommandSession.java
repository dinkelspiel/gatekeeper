package dev.keii.keiicore.commands;

import dev.keii.keiicore.KeiiCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandSession implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!KeiiCore.config.Invite)
        {
            sender.sendMessage(Component.text("Invites are not enabled on this server").color(NamedTextColor.RED));
            return true;
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage("§cYou must run this command as player!");
            return false;
        }

        sender.sendMessage(Component.text("Your session is ").color(NamedTextColor.YELLOW).append(Component.text(KeiiCore.Sessions.get(((Player) sender).getUniqueId().toString()))).clickEvent(ClickEvent.copyToClipboard(KeiiCore.Sessions.get(((Player) sender).getUniqueId().toString()))));

        return true;
    }
}