package dev.keii.gatekeeper.events;

import dev.keii.gatekeeper.Invite;
import dev.keii.gatekeeper.Gatekeeper;
import dev.keii.gatekeeper.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class PlayerJoin implements Listener {
    public static void loginUser(Player player, boolean showMessages)
    {
        boolean userExists = false;
        for(User user : Gatekeeper.users)
        {
            if(Objects.equals(user.getUuid(), player.getUniqueId().toString()))
            {
                userExists = true;
                break;
            }
        }

        if(!userExists)
        {
            boolean inviteExists = false;
            Invite invite = null;
            for(Invite _invite : Gatekeeper.invites)
            {
                if(Objects.equals(_invite.getInviteUUID(), player.getUniqueId().toString()))
                {
                    invite = _invite;
                    inviteExists = true;
                    break;
                }
            }

            if(!inviteExists)
            {
                player.kick(Component.text("You do not have an invite!").color(NamedTextColor.RED));
                return;
            }

            User invitor = null;
            for(User user : Gatekeeper.users)
            {
                if(user.getId() == invite.getUserId())
                {
                    invitor = user;
                    break;
                }
            }

            if(invitor == null)
            {
                player.kick(Component.text("Invitor doesn't exist contact an admin!").color(NamedTextColor.RED));
                return;
            }

            Gatekeeper.users.add(new User(++Gatekeeper.usersAutoIncrement, player.getUniqueId().toString(), invitor.getId()));

            if(showMessages)
                Bukkit.getServer().broadcast(Component.text("Everyone welcome " + player.getName() + " to the server").color(NamedTextColor.YELLOW));

            return;
        }

        if(showMessages)
            Bukkit.getServer().broadcast(Component.text(player.getName() + " joined the server").color(NamedTextColor.YELLOW));

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        if(Gatekeeper.disabled)
        {
            return;
        }

        Player player = event.getPlayer();
        event.joinMessage(Component.text(""));
        loginUser(player, true);
    }
}
