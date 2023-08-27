package dev.keii.gatekeeper.events;

import dev.keii.gatekeeper.DatabaseConnector;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.*;

public class PlayerJoin implements Listener {
    public static boolean loginUser(Player player, boolean showMessages)
    {
        try {
            Connection connection = DatabaseConnector.getConnection();

            PreparedStatement getUser = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
            getUser.setString(1, player.getUniqueId().toString());

            ResultSet user = getUser.executeQuery();

            if(!user.next())
            {
                PreparedStatement getInvite = connection.prepareStatement("SELECT * FROM invites WHERE invite_uuid = ?");
                getInvite.setString(1, player.getUniqueId().toString());

                ResultSet invite = getInvite.executeQuery();

                if(!invite.next())
                {
                    player.kick(Component.text("You do not have an invite!").color(NamedTextColor.RED));

                    invite.close();
                    user.close();
                    getUser.close();
                    getInvite.close();
                    connection.close();

                    return false;
                }

                PreparedStatement createUser = connection.prepareStatement("INSERT INTO users(uuid, recommended_by) VALUES(?, ?)");
                createUser.setString(1, player.getUniqueId().toString());
                createUser.setInt(2, invite.getInt("user_id"));
                createUser.execute();

                if(showMessages)
                    Bukkit.getServer().broadcast(Component.text("Everyone welcome " + player.getName() + " to the server").color(NamedTextColor.YELLOW));

                getUser.close();
                user.close();;
                getInvite.close();
                invite.close();
                createUser.close();
                connection.close();

                return true;
            }

            if(showMessages)
                Bukkit.getServer().broadcast(Component.text(player.getName() + " joined the server").color(NamedTextColor.YELLOW));

            getUser.close();
            user.close();;
            connection.close();
        } catch(SQLException e)
        {
            player.kick(Component.text("Login error!").color(NamedTextColor.RED).appendNewline().append(Component.text(e.getMessage()).color(NamedTextColor.RED)));

            return false;
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        event.joinMessage(Component.text(""));
        loginUser(player, true);
    }
}
