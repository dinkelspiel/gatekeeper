package dev.keii.keiicore;

import dev.keii.keiicore.api.ApiChunk;
import dev.keii.keiicore.error.Failure;
import dev.keii.keiicore.error.Result;
import dev.keii.keiicore.error.Success;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerChunk {
    static Map<Long, ApiChunk> claimCache = new HashMap<>();

    public static boolean getPlayerOwnsChunk(Player player, Chunk chunk) {
        try {
            Connection connection = DatabaseConnector.getConnection();
            Statement statement = connection.createStatement();

            String sessionQuery = "SELECT user_id FROM session WHERE session_id = \"" + KeiiCore.Sessions.get(player.getUniqueId().toString()) + "\"";
            ResultSet sessionResultSet = statement.executeQuery(sessionQuery);

            if (!sessionResultSet.next()) {
                sessionResultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            int userId = sessionResultSet.getInt("user_id");

            String query = "SELECT user_id FROM claim WHERE chunk_x = " + chunk.getX() + " AND chunk_z = " + +chunk.getZ();
            ResultSet resultSet = statement.executeQuery(query);

            if (!resultSet.next()) {
                sessionResultSet.close();
                resultSet.close();
                statement.close();
                connection.close();
                return false;
            }

//            Bukkit.broadcastMessage("DB: " + resultSet.getInt("user_id")  + " Local: " + userId);
            if (resultSet.getInt("user_id") != userId) {
                sessionResultSet.close();
                resultSet.close();
                statement.close();
                connection.close();
                return false;
            }
            // Close resources
            resultSet.close();
            statement.close();
            connection.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean getPlayerCanModifyChunk(Player player, Chunk chunk) {
        try {
            Connection connection = DatabaseConnector.getConnection();
            Statement statement = connection.createStatement();

            String sessionQuery = "SELECT user_id FROM session WHERE session_id = \"" + KeiiCore.Sessions.get(player.getUniqueId().toString()) + "\"";
            ResultSet sessionResultSet = statement.executeQuery(sessionQuery);

            if (!sessionResultSet.next()) {
                sessionResultSet.close();
                statement.close();
                connection.close();
                return true;
            }

            int userId = sessionResultSet.getInt("user_id");

            String query = "SELECT user_id FROM claim WHERE chunk_x = " + chunk.getX() + " AND chunk_z = " + +chunk.getZ();
            ResultSet resultSet = statement.executeQuery(query);

            if (!resultSet.next()) {
                sessionResultSet.close();
                resultSet.close();
                statement.close();
                connection.close();
                return true;
            }

//            Bukkit.broadcastMessage("DB: " + resultSet.getInt("user_id")  + " Local: " + userId);
            if (resultSet.getInt("user_id") != userId) {
                sessionResultSet.close();
                resultSet.close();
                statement.close();
                connection.close();
                return false;
            }
            // Close resources
            sessionResultSet.close();
            resultSet.close();
            statement.close();
            connection.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Nullable
    public static String getChunkOwner(Chunk chunk) {
        try {
            Connection connection = DatabaseConnector.getConnection();
            Statement statement = connection.createStatement();

            String query = "SELECT user_id, nickname FROM claim LEFT JOIN user ON user_id = user.id WHERE chunk_x = " + chunk.getX() + " AND chunk_z = " + +chunk.getZ();
            ResultSet resultSet = statement.executeQuery(query);

            if (!resultSet.next()) {
                resultSet.close();
                statement.close();
                connection.close();
                return null;
            }

            String nickname = resultSet.getString("nickname");

            // Close resources
            resultSet.close();
            statement.close();
            connection.close();
            return nickname;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean claimChunk(Player player, Chunk chunk) {
        if (getChunkOwner(chunk) != null) {
            return false;
        }

        try {
            Connection connection = DatabaseConnector.getConnection();
            Statement statement = connection.createStatement();

            String sessionQuery = "SELECT user_id FROM session WHERE session_id = \"" + KeiiCore.Sessions.get(player.getUniqueId().toString()) + "\"";
            ResultSet sessionResultSet = statement.executeQuery(sessionQuery);

            if (!sessionResultSet.next()) {
                sessionResultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            int userId = sessionResultSet.getInt("user_id");

            ResultSet claimPowerResultSet = statement.executeQuery("SELECT claim_power FROM user WHERE id = " + userId);

            claimPowerResultSet.next();

            Integer claimPower = claimPowerResultSet.getInt("claim_power");

            claimPowerResultSet.close();

            claimPowerResultSet = statement.executeQuery("SELECT COUNT(id) as count FROM `claim` WHERE user_id = " + userId);
            claimPowerResultSet.next();

            if (claimPowerResultSet.getInt("count") >= claimPower) {
                player.sendMessage(Component.text("Not enough power to claim chunk").color(NamedTextColor.RED));
                claimPowerResultSet.close();
                sessionResultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            String query = "INSERT INTO claim(user_id, chunk_x, chunk_z) VALUES(" + userId + ", " + chunk.getX() + ", " + chunk.getZ() + ")";
            statement.execute(query);

            showChunkPerimeter(player, chunk);
            var result = addChunkPermissionsForUser(player, null, chunk);

            if(result instanceof Failure)
            {
                player.sendMessage(result.getMessage());
            }

            sessionResultSet.close();
            statement.close();
            connection.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean unClaimChunk(Player player, Chunk chunk) {
        try {
            Connection connection = DatabaseConnector.getConnection();
            Statement statement = connection.createStatement();

            String sessionQuery = "SELECT user_id FROM session WHERE session_id = \"" + KeiiCore.Sessions.get(player.getUniqueId().toString()) + "\"";
            ResultSet sessionResultSet = statement.executeQuery(sessionQuery);

            if (!sessionResultSet.next()) {
                sessionResultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            int userId = sessionResultSet.getInt("user_id");

            sessionResultSet.close();

            String claimQuery = "SELECT * FROM claim WHERE chunk_x = " + chunk.getX() + " AND chunk_z = " + chunk.getZ();
            sessionResultSet = statement.executeQuery(claimQuery);

            if (!sessionResultSet.next()) {
                sessionResultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            Long claimId = sessionResultSet.getLong("id");

            if (sessionResultSet.getInt("user_id") != userId) {
                sessionResultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            statement.execute("DELETE FROM claim_permission WHERE claim_id = " + claimId);
            String query = "DELETE FROM claim WHERE user_id = " + userId + " AND chunk_x = " + chunk.getX() + " AND chunk_z = " + chunk.getZ();
            statement.execute(query);

            sessionResultSet.close();
            statement.close();
            connection.close();
            return true;
        } catch (SQLException e) {
            player.sendMessage(Component.text(e.getMessage()).color(NamedTextColor.RED));
            e.printStackTrace();
        }

        return false;
    }

    private static void showChunkPerimeter(Player player, Chunk chunk) {
        int worldX = chunk.getX() * 16;
        int worldZ = chunk.getZ() * 16;

        HashMap<Location, BlockData> blockChanges = new HashMap<>();

        for (var i = 0; i < 16; i++) {
            int x = worldX + i;
            int z = worldZ;
            int y = chunk.getWorld().getHighestBlockYAt(x, z);

            BlockData bd = Material.GOLD_BLOCK.createBlockData();

            blockChanges.put(new Location(chunk.getWorld(), x, y, z), bd);
        }

        for (var i = 0; i < 15; i++) {
            int x = worldX + i;
            int z = worldZ + 15;
            int y = chunk.getWorld().getHighestBlockYAt(x, z);

            BlockData bd = Material.GOLD_BLOCK.createBlockData();

            blockChanges.put(new Location(chunk.getWorld(), x, y, z), bd);
        }

        for (var i = 0; i < 15; i++) {
            int x = worldX;
            int z = worldZ + i;
            int y = chunk.getWorld().getHighestBlockYAt(x, z);

            BlockData bd = Material.GOLD_BLOCK.createBlockData();

            blockChanges.put(new Location(chunk.getWorld(), x, y, z), bd);
        }

        for (var i = 0; i < 16; i++) {
            int x = worldX + 15;
            int z = worldZ + i;
            int y = chunk.getWorld().getHighestBlockYAt(x, z);

            BlockData bd = Material.GOLD_BLOCK.createBlockData();

            blockChanges.put(new Location(chunk.getWorld(), x, y, z), bd);
        }

        player.sendMultiBlockChange(blockChanges);
    }

    public static boolean toggleExplosionPolicy(Player player, Chunk chunk) {
        try {
            Connection connection = DatabaseConnector.getConnection();
            Statement statement = connection.createStatement();

            String sessionQuery = "SELECT user_id FROM session WHERE session_id = \"" + KeiiCore.Sessions.get(player.getUniqueId().toString()) + "\"";
            ResultSet sessionResultSet = statement.executeQuery(sessionQuery);

            if (!sessionResultSet.next()) {
                sessionResultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            int userId = sessionResultSet.getInt("user_id");

            sessionResultSet.close();

            String claimQuery = "SELECT * FROM claim WHERE chunk_x = " + chunk.getX() + " AND chunk_z = " + chunk.getZ();
            sessionResultSet = statement.executeQuery(claimQuery);

            if (!sessionResultSet.next()) {
                sessionResultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            long id = sessionResultSet.getLong("id");
            long user_id = sessionResultSet.getLong("user_id");
            int chunk_x = sessionResultSet.getInt("chunk_x");
            int chunk_z = sessionResultSet.getInt("chunk_z");
            Timestamp created_at = sessionResultSet.getTimestamp("created_at");
            Timestamp updated_at = sessionResultSet.getTimestamp("updated_at");

            if (sessionResultSet.getInt("user_id") != userId) {
                sessionResultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            int newExp = (sessionResultSet.getBoolean("allow_explosions") ? 0 : 1);

            String query = "UPDATE claim SET allow_explosions = " + newExp + ", updated_at = current_timestamp() WHERE user_id = " + userId + " AND chunk_x = " + chunk.getX() + " AND chunk_z = " + chunk.getZ();
            statement.execute(query);

            ApiChunk c = claimCache.get(chunk.getChunkKey());
            if (c != null) {
                c.setAllowExplosions(newExp == 1);
                claimCache.remove(chunk.getChunkKey());
                claimCache.put(chunk.getChunkKey(), c);
            } else {
                claimCache.put(chunk.getChunkKey(), new ApiChunk(
                        id,
                        user_id,
                        chunk_x,
                        chunk_z,
                        created_at,
                        updated_at,
                        newExp == 1
                ));
            }

            sessionResultSet.close();
            statement.close();
            connection.close();
            return true;
        } catch (SQLException e) {
            player.sendMessage(Component.text(e.getMessage()).color(NamedTextColor.RED));
            e.printStackTrace();
        }

        return false;
    }

    public static boolean getExplosionPolicy(Chunk chunk) {
        ApiChunk cache = claimCache.get(chunk.getChunkKey());
        if (cache != null) {
            return cache.isAllowExplosions();
        }

        try {
            Connection connection = DatabaseConnector.getConnection();
            Statement statement = connection.createStatement();

            String claimQuery = "SELECT * FROM claim WHERE chunk_x = " + chunk.getX() + " AND chunk_z = " + chunk.getZ();
            ResultSet resultSet = statement.executeQuery(claimQuery);

            if (!resultSet.next()) {
                resultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            boolean allow = resultSet.getBoolean("allow_explosions");

            claimCache.put(chunk.getChunkKey(), new ApiChunk(
                    resultSet.getLong("id"),
                    resultSet.getLong("user_id"),
                    resultSet.getInt("chunk_x"),
                    resultSet.getInt("chunk_z"),
                    resultSet.getTimestamp("created_at"),
                    resultSet.getTimestamp("updated_at"),
                    resultSet.getBoolean("allow_explosions")
            ));

            resultSet.close();
            statement.close();
            connection.close();
            return allow;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public enum ChunkPermission {
        BlockBreak,
        BlockPlace,
        BucketEmpty,
        BucketFill,
        Interact
    }

    public static String getChunkPermissionString(ChunkPermission perm)
    {
        switch(perm) {
            case BucketEmpty -> {
                return "bucket_empty";
            }
            case BucketFill -> {
                return "bucket_fill";
            }
            case Interact -> {
                return "interact";
            }
            case BlockBreak -> {
                return "block_break";
            }
            case BlockPlace -> {
                return "block_place";
            }
        }
        return "";
    }

    public static boolean setClaimPermission(Player player, Chunk chunk, @Nullable String targetPlayerUUID, ChunkPermission permission, boolean value)
    {
        try {
            Connection connection = DatabaseConnector.getConnection();
            Statement statement = connection.createStatement();

            String sessionQuery = "SELECT user_id FROM session WHERE session_id = \"" + KeiiCore.Sessions.get(player.getUniqueId().toString()) + "\"";
            ResultSet sessionResultSet = statement.executeQuery(sessionQuery);

            if (!sessionResultSet.next()) {
                sessionResultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            int userId = sessionResultSet.getInt("user_id");

            sessionResultSet.close();

            String claimQuery = "SELECT * FROM claim WHERE chunk_x = " + chunk.getX() + " AND chunk_z = " + chunk.getZ();
            ResultSet claimResultSet = statement.executeQuery(claimQuery);

            if (!claimResultSet.next()) {
                claimResultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            if (claimResultSet.getInt("user_id") != userId) {
                claimResultSet.close();
                statement.close();
                connection.close();
                return false;
            }

            Long claimId = claimResultSet.getLong("id");
            Long targetId = null;
            if(targetPlayerUUID != null) {
                String targetQuery = "SELECT * FROM user WHERE uuid = \"" + targetPlayerUUID + "\"";
                ResultSet targetResultSet = statement.executeQuery(targetQuery);

                if (!targetResultSet.next()) {
                    targetResultSet.close();
                    claimResultSet.close();
                    statement.close();
                    connection.close();
                    return false;
                }

                targetId = targetResultSet.getLong("id");
            }

            String claimPermissionQuery;
            if(targetId != null) {
                claimPermissionQuery = "SELECT * FROM claim_permission WHERE user_id = " + String.valueOf(targetId) + " AND claim_id = " + claimId;
            } else {
                claimPermissionQuery = "SELECT * FROM claim_permission WHERE user_id IS NULL AND claim_id = " + claimId;
            }
            ResultSet claimPermissionResultSet = statement.executeQuery(claimPermissionQuery);

            if(!claimPermissionResultSet.next())
            {
                if(targetId != null) {
                    statement.execute("INSERT INTO claim_permission(claim_id, user_id) VALUES(" + claimId + ", " + String.valueOf(targetId) + ")");
                } else {
                    statement.execute("INSERT INTO claim_permission(claim_id, user_id) VALUES(" + claimId + ", NULL)");
                }
            }

            String permissionString = "";
            switch(permission) {
                case Interact -> permissionString = "interact";
                case BlockBreak -> permissionString = "block_break";
                case BlockPlace -> permissionString = "block_place";
                case BucketFill -> permissionString = "bucket_fill";
                case BucketEmpty -> permissionString = "bucket_empty";
            }

            String updateQuery;
            if(targetId != null) {
                updateQuery = "UPDATE claim_permission SET " + permissionString + " = " + (value ? 1 : 0) + " WHERE user_id = " + String.valueOf(targetId) + " AND claim_id = " + claimId;
            } else {
                updateQuery = "UPDATE claim_permission SET " + permissionString + " = " + (value ? 1 : 0) + " WHERE user_id IS NULL AND claim_id = " + claimId;
            }
            statement.execute(updateQuery);

            claimPermissionResultSet.close();
            claimResultSet.close();
            sessionResultSet.close();
            statement.close();
            connection.close();
            return true;
        } catch (SQLException e) {
            player.sendMessage(Component.text(e.getMessage()).color(NamedTextColor.RED));
            e.printStackTrace();
        }

        return false;
    }

    @Nullable
    public static Map<ChunkPermission, Boolean> getChunkPermissionsForUser(String uuid, Chunk chunk)
    {
        try {
            Connection connection = DatabaseConnector.getConnection();
            Statement statement = connection.createStatement();

            String claimQuery = "SELECT * FROM claim WHERE chunk_x = " + chunk.getX() + " AND chunk_z = " + chunk.getZ();
            ResultSet claimResultSet = statement.executeQuery(claimQuery);

            if (!claimResultSet.next()) {
                claimResultSet.close();
                statement.close();
                connection.close();
                Bukkit.getConsoleSender().sendMessage("No Claim");
                return null;
            }

            long claimId = claimResultSet.getLong("id");
            claimResultSet.close();

            if(uuid == null)
            {

                ResultSet claimPermissionResultSet = statement.executeQuery("SELECT * FROM claim_permission WHERE user_id IS NULL AND claim_id = " + claimId);

                if (!claimPermissionResultSet.next()) {
                    Bukkit.getConsoleSender().sendMessage("No Claim Permission");

                    claimPermissionResultSet.close();
                    claimResultSet.close();
                    statement.close();
                    connection.close();
                    return null;
                }

                Boolean bb = claimPermissionResultSet.getBoolean("block_break");
                Boolean bp = claimPermissionResultSet.getBoolean("block_place");
                Boolean be = claimPermissionResultSet.getBoolean("bucket_empty");
                Boolean bf = claimPermissionResultSet.getBoolean("bucket_fill");
                Boolean in = claimPermissionResultSet.getBoolean("interact");

                Map<ChunkPermission, Boolean> perm = new HashMap<>();
                perm.put(ChunkPermission.BlockBreak, bb);
                perm.put(ChunkPermission.BlockPlace, bp);
                perm.put(ChunkPermission.BucketEmpty, be);
                perm.put(ChunkPermission.BucketFill, bf);
                perm.put(ChunkPermission.Interact, in);

                claimPermissionResultSet.close();
                claimResultSet.close();
                statement.close();
                connection.close();
                return perm;
            }

            String sql = "SELECT * FROM user WHERE uuid = \"" + uuid + "\"";
            ResultSet userResultSet = statement.executeQuery(sql);

            if (!userResultSet.next()) {
                Bukkit.getConsoleSender().sendMessage("No User" + sql);

                userResultSet.close();
                claimResultSet.close();
                statement.close();
                connection.close();
                return null;
            }

            long userId = userResultSet.getLong("id");
            userResultSet.close();

            ResultSet claimPermissionResultSet = statement.executeQuery("SELECT * FROM claim_permission WHERE user_id = " + userId + " AND claim_id = " + claimId);

            if (!claimPermissionResultSet.next()) {
                Bukkit.getConsoleSender().sendMessage("No Claim Permission");

                claimPermissionResultSet.close();
                userResultSet.close();
                claimResultSet.close();
                statement.close();
                connection.close();
                return null;
            }

            Boolean bb = claimPermissionResultSet.getBoolean("block_break");
            Boolean bp = claimPermissionResultSet.getBoolean("block_place");
            Boolean be = claimPermissionResultSet.getBoolean("bucket_empty");
            Boolean bf = claimPermissionResultSet.getBoolean("bucket_fill");
            Boolean in = claimPermissionResultSet.getBoolean("interact");

            Map<ChunkPermission, Boolean> perm = new HashMap<>();
            perm.put(ChunkPermission.BlockBreak, bb);
            perm.put(ChunkPermission.BlockPlace, bp);
            perm.put(ChunkPermission.BucketEmpty, be);
            perm.put(ChunkPermission.BucketFill, bf);
            perm.put(ChunkPermission.Interact, in);

            claimPermissionResultSet.close();
            userResultSet.close();
            claimResultSet.close();
            statement.close();
            connection.close();
            return perm;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getConsoleSender().sendMessage("Found error");

        return null;
    }

    public static Result addChunkPermissionsForUser(Player player, @Nullable  String targetNickname, Chunk chunk)
    {
        try {
            Connection connection = DatabaseConnector.getConnection();
            Statement statement = connection.createStatement();

            String claimQuery = "SELECT * FROM claim WHERE chunk_x = " + chunk.getX() + " AND chunk_z = " + chunk.getZ();
            ResultSet claimResultSet = statement.executeQuery(claimQuery);

            if (!claimResultSet.next()) {
                claimResultSet.close();
                statement.close();
                connection.close();
//                Bukkit.getConsoleSender().sendMessage("No Claim");
                return new Failure("No claim at chunk");
            }

            long claimOwnerId = claimResultSet.getLong("user_id");
            long claimId = claimResultSet.getLong("id");
            claimResultSet.close();

            String sql = "SELECT * FROM user WHERE uuid = \"" + player.getUniqueId().toString() + "\"";
            ResultSet userResultSet = statement.executeQuery(sql);

            if (!userResultSet.next()) {
//                Bukkit.getConsoleSender().sendMessage("No User");

                userResultSet.close();
                claimResultSet.close();
                statement.close();
                connection.close();
                return new Failure("No user exists with uuid");
            }

            long userId = userResultSet.getLong("id");
            userResultSet.close();

            if(claimOwnerId != userId)
            {
//                player.sendMessage(Component.text("No permission to update chunk").color(NamedTextColor.RED));

                statement.close();
                connection.close();
                return new Failure("Player doesn't have permission to update chunk");
            }

            if(targetNickname == null)
            {
                Statement checkPermStatement = connection.createStatement();
                ResultSet chunkPermissionRS = checkPermStatement.executeQuery("SELECT 1 FROM claim_permission WHERE user_id IS NULL AND claim_id = " + claimId);

                if(chunkPermissionRS.next())
                {
                    chunkPermissionRS.close();
                    checkPermStatement.close();
                    statement.close();
                    connection.close();
                    return new Failure("Everyone already exists on chunk");
                }

                chunkPermissionRS.close();

                statement.execute("INSERT INTO claim_permission(user_id, claim_id) VALUES(NULL, " + claimId + ")");

                statement.close();
                connection.close();
                return new Success("Created everyone permission");
            }

            String targetSql = "SELECT * FROM user WHERE UPPER(nickname) = UPPER(\"" + targetNickname + "\")";
            ResultSet targetResultSet = statement.executeQuery(targetSql);

            if (!targetResultSet.next()) {
//                Bukkit.getConsoleSender().sendMessage("No Target");

                targetResultSet.close();
                statement.close();
                connection.close();
                return new Failure("No target with nickname");
            }

            long targetUserId = targetResultSet.getLong("id");
            targetResultSet.close();

            if(targetUserId == userId)
            {
//                Bukkit.getConsoleSender().sendMessage("Can't be same as user");

                statement.close();
                connection.close();
                return new Failure("Target can't be the same as user");
            }

            ResultSet chunkPermissionRS = statement.executeQuery("SELECT 1 FROM claim_permission WHERE user_id = " + targetUserId + " AND claim_id = " + claimId);

            if(chunkPermissionRS.next())
            {
                chunkPermissionRS.close();
                statement.close();
                connection.close();
                return new Failure("Target permission already exists on chunk");
            }

            chunkPermissionRS.close();

            statement.execute("INSERT INTO claim_permission(user_id, claim_id) VALUES(" + targetUserId + ", " + claimId + ")");

            statement.close();
            connection.close();
            return new Success("Created permission");
        } catch (SQLException e) {
            e.printStackTrace();
        }

//        Bukkit.getConsoleSender().sendMessage("Found error");

        return new Failure("Internal error");
    }
}
