package dev.keii.gatekeeper;

import dev.keii.gatekeeper.commands.CommandGatekeeper;
import dev.keii.gatekeeper.commands.CommandInvite;
import dev.keii.gatekeeper.events.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class Gatekeeper extends JavaPlugin {

    private static Gatekeeper instance;

    public static List<User> users = new ArrayList<>();
    public static int usersAutoIncrement = 1;
    public static List<Invite> invites = new ArrayList<>();

    public static boolean disabled = false;

    @Override
    public void onEnable() {
        instance = this;

        load(null);

        registerEvents();
        registerCommands();

        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            PlayerJoin.loginUser(player, false);
        }
    }

    @Override
    public void onDisable()
    {
        save(null);
    }

    public void registerEvents() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(new PlayerJoin(), this);
        pm.registerEvents(new PlayerQuit(), this);
    }

    public void registerCommands() {
        this.getCommand("invite").setExecutor(new CommandInvite());
        this.getCommand("gatekeeper").setExecutor(new CommandGatekeeper());
    }

    public static Gatekeeper getInstance()
    {
        return instance;
    }

    public static void save(@Nullable  CommandSender sender) {
        if (sender != null) {
            if (!sender.hasPermission("keii.gatekeeper.save")) {
                sender.sendMessage(Component.text("No permission").color(NamedTextColor.RED));
                return;
            }
        }

        Component text = Component.text("Saving Gatekeeper!").color(NamedTextColor.YELLOW);

        Bukkit.getConsoleSender().sendMessage(text);
        for(Player player : Bukkit.getOnlinePlayers())
        {
            if(player.hasPermission("keii.gatekeeper.save"))
            {
                player.sendMessage(text);
            }
        }

        Connection connection = null;

        try {
            connection = DatabaseConnector.getConnection();

            DatabaseConnector.initializeDatabase(connection);

            Statement statement = connection.createStatement();

            statement.execute("DELETE FROM users;");
            statement.execute("DELETE FROM invites;");

            for(User user : Gatekeeper.users) {
                statement.execute("INSERT INTO users(id, uuid, recommended_by) VALUES(" + user.getId() + ", '" + user.getUuid() + "', " + user.getRecommendedBy() + ")");
            }

            for(Invite invite : Gatekeeper.invites) {
                statement.execute("INSERT INTO invites(user_id, invite_uuid, used) VALUES(" + invite.getUserId() + ", '" + invite.getInviteUUID() + "', " + (invite.isUsed() ? 1 : 0) + ")");
            }

            statement.close();
            connection.close();
        } catch(SQLException e)
        {
            text = Component.text("Failed saving Gatekeeper! " + e.getMessage()).color(NamedTextColor.RED);
            if(sender != null)
                sender.sendMessage(text);
            else {
                Bukkit.getConsoleSender().sendMessage(text);
                for(Player player : Bukkit.getOnlinePlayers())
                {
                    if(player.hasPermission("keii.gatekeeper.save"))
                    {
                        player.sendMessage(text);
                    }
                }
            }
            return;
        }

        text = Component.text("Finished saving Gatekeeper!").color(NamedTextColor.GREEN);

        Bukkit.getConsoleSender().sendMessage(text);
        for(Player player : Bukkit.getOnlinePlayers())
        {
            if(player.hasPermission("keii.gatekeeper.save"))
            {
                player.sendMessage(text);
            }
        }
    }

    public static void load(@Nullable CommandSender sender)
    {
        if(sender != null) {
            if (!sender.hasPermission("keii.gatekeeper.load")) {
                sender.sendMessage(Component.text("No permission").color(NamedTextColor.RED));
                return;
            }
        }

        Component text = Component.text("Loading Gatekeeper!").color(NamedTextColor.YELLOW);

        Bukkit.getConsoleSender().sendMessage(text);
        for(Player player : Bukkit.getOnlinePlayers())
        {
            if(player.hasPermission("keii.gatekeeper.load"))
            {
                player.sendMessage(text);
            }
        }

        Connection connection = null;

        try {
            connection = DatabaseConnector.getConnection();
            connection.setAutoCommit(false);

            DatabaseConnector.initializeDatabase(connection);

            Statement statement = connection.createStatement();

            ResultSet users = statement.executeQuery("SELECT * FROM users");

            Gatekeeper.users.clear();
            while(users.next()) {
                int id = users.getInt("id");
                String uuid = users.getString("uuid");
                int recommended_by = users.getInt("recommended_by");

                Gatekeeper.users.add(new User(id, uuid, recommended_by));
            }

            users.close();
            ResultSet invites = statement.executeQuery("SELECT * FROM invites");

            Gatekeeper.invites.clear();
            while(invites.next()) {
                int userId = invites.getInt("user_id");
                String inviteUUID = invites.getString("invite_uuid");
                boolean used = invites.getBoolean("used");

                Gatekeeper.invites.add(new Invite(userId, inviteUUID, used));
            }

            invites.close();

            connection.commit();

            statement.close();
            connection.close();
        } catch(SQLException e)
        {
            if(connection != null)
            {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    text = Component.text("Failed Gatekeeper load rollback! " + e.getMessage()).color(NamedTextColor.RED);
                    if(sender != null)
                        sender.sendMessage(text);
                    else {
                        Bukkit.getConsoleSender().sendMessage(text);
                        for(Player player : Bukkit.getOnlinePlayers())
                        {
                            if(player.hasPermission("keii.gatekeeper.load"))
                            {
                                player.sendMessage(text);
                            }
                        }
                    }
                }
            }

            text = Component.text("Failed loading Gatekeeper! " + e.getMessage()).color(NamedTextColor.RED);
            if(sender != null)
                sender.sendMessage(text);
            else {
                Bukkit.getConsoleSender().sendMessage(text);
                for(Player player : Bukkit.getOnlinePlayers())
                {
                    if(player.hasPermission("keii.gatekeeper.load"))
                    {
                        player.sendMessage(text);
                    }
                }
            }
            return;
        }

        text = Component.text("Finished loading Gatekeeper!").color(NamedTextColor.GREEN);

        Bukkit.getConsoleSender().sendMessage(text);
        for(Player player : Bukkit.getOnlinePlayers())
        {
            if(player.hasPermission("keii.gatekeeper.load"))
            {
                player.sendMessage(text);
            }
        }
    }
}
