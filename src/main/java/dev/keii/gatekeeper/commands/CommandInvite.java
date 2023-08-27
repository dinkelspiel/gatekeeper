package dev.keii.gatekeeper.commands;

import dev.keii.gatekeeper.DatabaseConnector;
import dev.keii.gatekeeper.Gatekeeper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CommandInvite implements CommandExecutor {
    static class ApiResponse {
        String code;
        String message;
        PlayerDataWrapper data;
        boolean success;
    }

    static class PlayerDataWrapper {
        PlayerData player;
    }

    static class PlayerData {
        PlayerMeta meta;
        String username;
        String id;
        String raw_id;
        String avatar;
        // Other fields as needed
    }

    static class PlayerMeta {
        long cached_at;
    }

     @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("You must run this command as player!").color(NamedTextColor.RED));
            return false;
        }

         if(args.length < 1)
         {
             sender.sendMessage("§cNo username provided");
             return false;
         }

         OkHttpClient client = new OkHttpClient();
         Gson gson = new Gson();

         Request request = new Request.Builder()
                 .url("https://playerdb.co/api/player/minecraft/" + args[0])
                 .build();

         String playerUsername;
         String playerUUID;

         try {
             Response response = client.newCall(request).execute();
             String responseBody = response.body().string();
             ApiResponse apiResponse = gson.fromJson(responseBody, ApiResponse.class);
             if (apiResponse.success) {
                 PlayerData playerData = apiResponse.data.player;
                 playerUsername = playerData.username;
                 playerUUID = playerData.id;
             } else {
                 player.sendMessage(Component.text("Error while inviting user: " + apiResponse.message).color(NamedTextColor.RED));
                 return true;
             }
         } catch (Exception e)
         {
             player.sendMessage(Component.text("Error while inviting user: " + e.getMessage()).color(NamedTextColor.RED));
             return true;
         }

         try {
            Connection connection = DatabaseConnector.getConnection();

            PreparedStatement getInvitor = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?");
            getInvitor.setString(1, player.getUniqueId().toString());

            ResultSet invitor = getInvitor.executeQuery();

            if(!invitor.next())
            {
                player.sendMessage(Component.text("No valid user with your uuid! Contact an administrator!").color(NamedTextColor.RED));

                getInvitor.close();
                invitor.close();

                return true;
            }

            PreparedStatement getInvite = connection.prepareStatement("SELECT * FROM invites WHERE invite_uuid = ?");
            getInvite.setString(1, playerUUID);

            ResultSet invite = getInvite.executeQuery();

            if(invite.next())
            {
                player.sendMessage(Component.text("An invite already exists for this user").color(NamedTextColor.RED));

                getInvitor.close();
                invitor.close();
                getInvite.close();
                invite.close();

                return true;
            }

            PreparedStatement createInvite = connection.prepareStatement("INSERT INTO invites(user_id, invite_uuid) VALUES(?, ?)");
            createInvite.setInt(1, invitor.getInt("id"));
            createInvite.setString(2, playerUUID);
            createInvite.execute();

            player.sendMessage(Component.text("Invited user").color(NamedTextColor.GREEN));

            getInvitor.close();
            invitor.close();
            getInvite.close();
            invite.close();
            createInvite.close();

            return true;
        } catch(SQLException e)
        {
            player.sendMessage(Component.text("Error while inviting user: " + e.getMessage()).color(NamedTextColor.RED));
            return true;
        }
    }
}