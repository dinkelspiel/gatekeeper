package dev.keii.keiicore.commands;

import dev.keii.keiicore.KeiiCore;
import dev.keii.keiicore.inventories.InventoryMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandMap implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!KeiiCore.config.Claims)
        {
            sender.sendMessage(Component.text("Claims are not enabled on this server").color(NamedTextColor.RED));
            return true;
        }

        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("You must run this command as player!").color(NamedTextColor.RED));
            return false;
        }

        InventoryMap map = new InventoryMap(player);
        player.openInventory(map.getInventory());

        return true;
    }
}