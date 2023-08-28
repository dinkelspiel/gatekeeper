package dev.keii.gatekeeper.commands;

import dev.keii.gatekeeper.Gatekeeper;
import dev.keii.gatekeeper.Invite;
import dev.keii.gatekeeper.User;
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
import java.text.SimpleDateFormat;
import java.util.Objects;


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

         if(Gatekeeper.disabled)
         {
             sender.sendMessage(Component.text("Gatekeeper is disabled!").color(NamedTextColor.RED));
             if(player.hasPermission("keii.gatekeeper.reload"))
             {
                 sender.sendMessage(Component.text("Run '/gatekeeper reload' to reload the config!").color(NamedTextColor.RED));
             }
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

         String playerUUID;

         try {
             Response response = client.newCall(request).execute();
             String responseBody = response.body().string();
             ApiResponse apiResponse = gson.fromJson(responseBody, ApiResponse.class);
             if (apiResponse.success) {
                 PlayerData playerData = apiResponse.data.player;
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

         User user = null;
         boolean userExists = false;

         for(User _user : Gatekeeper.users)
         {
            if(Objects.equals(_user.getUuid(), player.getUniqueId().toString()))
            {
                user = _user;
                userExists = true;
                break;
            }
         }

         if(!userExists)
         {
             player.sendMessage(Component.text("No valid user with your uuid! Contact an administrator!").color(NamedTextColor.RED));
             return true;
         }

         boolean inviteExists = false;

         for(Invite invite : Gatekeeper.invites)
         {
             if(Objects.equals(invite.getInviteUUID(), playerUUID))
             {
                 inviteExists = true;
                 break;
             }
         }

         if(inviteExists)
         {
             player.sendMessage(Component.text("An invite already exists for this user!").color(NamedTextColor.RED));
             return true;
         }

         Gatekeeper.invites.add(new Invite(user.getId(), playerUUID, false));
         player.sendMessage(Component.text("Invited user").color(NamedTextColor.GREEN));

         return true;
    }
}