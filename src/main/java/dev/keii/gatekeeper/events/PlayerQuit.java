package dev.keii.gatekeeper.events;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        event.quitMessage(Component.text(event.getPlayer().getName() + " left the server").color(NamedTextColor.YELLOW));
    }
}
