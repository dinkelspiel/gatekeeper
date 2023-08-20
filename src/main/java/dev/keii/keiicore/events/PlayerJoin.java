package dev.keii.keiicore.events;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.keii.keiicore.KeiiCore;
import dev.keii.keiicore.RuntimeError;
import dev.keii.keiicore.api.ApiLogin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import okhttp3.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Set;

public class PlayerJoin implements Listener {
    public static void loginUser(Player player, boolean sendMessages)
    {
        if(KeiiCore.config.Claims) {
            String resourcePackURL = "https://github.com/shykeiichi/plugin-resourcepack/raw/main/release.zip";
            player.setResourcePack(resourcePackURL);
        }

        if(!KeiiCore.config.Invite)
        {
            return;
        }

        OkHttpClient client = new OkHttpClient();

        String jsonData = "{\"uuid\": \"" + player.getUniqueId() + "\"}";

        // Set the media type to application/json
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        // Create the request body with the JSON data and media type
        RequestBody requestBody = RequestBody.create(jsonData, mediaType);

//        String url = "https://mc.keii.dev/api/auth/login";
        String url = KeiiCore.config.ApiUrl + "/api/auth/login";

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            String responseBody = response.body().string();
            //Bukkit.getServer().broadcastMessage(responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            ApiLogin json = objectMapper.readValue(responseBody, ApiLogin.class);

            // Access the values in the ApiResponse object
            String nickname = json.getNickname();
            String session = json.getSession();

            if(json.getStatus() == 400)
            {
                player.kick(Component.text(json.getMessage()));
                return;
            }

            if(json.getStatus() == 201)
            {
                if(sendMessages) {
                    Bukkit.broadcast(Component.text("Everyone welcome " + player.displayName() + " to the server!").color(NamedTextColor.YELLOW));
                    player.sendMessage(Component.text("Welcome to the server!"));
                    player.sendMessage(Component.text("You can customize your character by running /user!"));
                }
                KeiiCore.Sessions.put(player.getUniqueId().toString(), session);
                return;
            }

            if(json.getStatus() == 200)
            {
                KeiiCore.Sessions.put(player.getUniqueId().toString(), session);
//                Bukkit.getServer().broadcastMessage(KeiiCore.Sessions.get(player.getUniqueId().toString()));

                PlayerProfile oldProfile = player.getPlayerProfile();
                Set<ProfileProperty> old = oldProfile.getProperties();
                PlayerProfile profile = Bukkit.createProfileExact(player.getUniqueId(), nickname);
                profile.setProperties(old); // The players previous properties
                player.setPlayerProfile(profile);

                if(sendMessages) {
                    Bukkit.broadcast(Component.text(nickname + " joined the server").color(NamedTextColor.YELLOW));
                }

                String resourcePackURL = "https://github.com/shykeiichi/plugin-resourcepack/raw/main/release.zip";
                player.setResourcePack(resourcePackURL);
                
                return;
            }

            player.kick(Component.text("Invalid status code! Contact an administrator\n").color(NamedTextColor.RED).append(Component.text(json.getStatus())));
        } catch (Exception e) {
            player.kick(Component.text("Login Failed!\n").color(NamedTextColor.RED).append(Component.text(e.getMessage())));
            KeiiCore.RuntimeErrors.add(new RuntimeError(e, player.getUniqueId().toString()));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        event.joinMessage(Component.text(""));
        loginUser(player, true);
    }
}
