package dev.keii.keiicore.events;

import dev.keii.keiicore.KeiiCore;
import dev.keii.keiicore.PlayerChunk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteract implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(!KeiiCore.config.Claims)
        {
            return;
        }

        Player player = event.getPlayer();
        Chunk chunk = player.getChunk();

        boolean canBreak = BlockBreak.getPlayerPermissionForChunk(player, chunk, PlayerChunk.ChunkPermission.Interact);

        event.setCancelled(!canBreak);
        if(!canBreak)
        {
            player.sendActionBar(Component.text("You do not have the rights to interact with this chunk!").color(NamedTextColor.RED));
        }
    }
}
