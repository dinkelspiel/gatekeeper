package dev.keii.keiicore.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.keii.keiicore.KeiiCore;
import dev.keii.keiicore.RuntimeError;
import dev.keii.keiicore.api.ApiStatusMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import okhttp3.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandInvite implements CommandExecutor {

     @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
         if(!KeiiCore.config.Invite)
         {
             sender.sendMessage(Component.text("Invites are not enabled on this server").color(NamedTextColor.RED));
             return true;
         }

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

        String jsonData = "{\"session\": \"" + KeiiCore.Sessions.get(player.getUniqueId().toString()) + "\", \"username\": \"" + args[0] + "\"}";

        // Set the media type to application/json
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        // Create the request body with the JSON data and media type
        RequestBody requestBody = RequestBody.create(jsonData, mediaType);

//        String url = "https://mc.keii.dev/api/invite";
        String url = KeiiCore.config.ApiUrl + "/api/invite";

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            String responseBody = response.body().string();
//            Bukkit.getServer().broadcastMessage(responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            ApiStatusMessage json = objectMapper.readValue(responseBody, ApiStatusMessage.class);

            // Access the values in the ApiResponse object
            int status = json.getStatus();
            String message = json.getMessage();

            if(status == 400)
            {
                sender.sendMessage("§c" + message);
            } else {
                sender.sendMessage("§a" + message);
            }
        } catch(Exception e) {
            sender.sendMessage(Component.text("An error occurred! Contact an administrator").color(NamedTextColor.RED));
            sender.sendMessage(Component.text(e.getMessage()).color(NamedTextColor.RED));
            KeiiCore.RuntimeErrors.add(new RuntimeError(e, player.getUniqueId().toString()));
        }

        return true;
    }
}