package dev.keii.gatekeeper.events;
import dev.keii.gatekeeper.Gatekeeper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if(Gatekeeper.disabled)
        {
            return;
        }

        if(event.getReason() == PlayerQuitEvent.QuitReason.KICKED)
        {
            event.quitMessage(Component.text(""));
            return;
        }

        event.quitMessage(Component.text(event.getPlayer().getName() + " left the server").color(NamedTextColor.YELLOW));
    }
}
