package net.slimediamond.espial;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Database {
    private String connectionString;
    private Connection conn;

    private PreparedStatement insertAction;
    private PreparedStatement queryCoords;
    private PreparedStatement queryId;
    private PreparedStatement queryRange;

    public Database(String connectionString) {
        this.connectionString = connectionString;
    }

    public void open() throws SQLException {
        conn = DriverManager.getConnection(connectionString);

        insertAction = conn.prepareStatement("INSERT INTO blocklog (type, time, player_uuid, block_id, world, x, y, z, player_x, player_y, player_z, player_pitch, player_yaw, player_roll, player_tool) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        queryCoords = conn.prepareStatement("SELECT * FROM blocklog WHERE world = ? AND x = ? AND y = ? AND z = ?");
        queryId = conn.prepareStatement("SELECT * FROM blocklog WHERE id = ?");
        queryRange = conn.prepareStatement("SELECT * FROM blocklog WHERE x BETWEEN ? and ? AND y BETWEEN ? and ? AND z BETWEEN ? AND ?");

        String sql;

        // Databases need to be made differently on different databases.
        if (connectionString.contains("sqlite")) {
            sql = "CREATE TABLE IF NOT EXISTS blocklog (id INTEGER PRIMARY KEY AUTOINCREMENT, ";
        } else {
            // Probably MySQL/MariaDB or whatever. use a different statement
            sql = "CREATE TABLE IF NOT EXISTS blocklog (id INT AUTO_INCREMENT PRIMARY KEY, ";
        }

        conn.prepareStatement(sql +
            "type TINYINT, " +
            "time TIMESTAMP, " +
            "player_uuid MEDIUMTEXT, " +
            "block_id TINYTEXT, " +
            "world TINYTEXT, " +
            "x INT, " +
            "y INT, " +
            "z INT, " +
            "player_x DOUBLE, " +
            "player_y DOUBLE, " +
            "player_z DOUBLE, " +
            "player_pitch DOUBLE, " +
            "player_yaw DOUBLE, " +
            "player_roll DOUBLE, " +
            "player_tool TINYTEXT" +
            ")"
        ).execute();
    }

    /**
     * Insert an action
     * @param type Type of action
     * @param player The player whom did the action
     * @param world The world which the action happened in
     * @param transaction Block transaction (for ChangeBlockEvent)
     * @param blockSnapshot Block snapshot (for InteractBlockEvent)
     * @throws SQLException
     */
    public void insertAction(@NonNull ActionType type, @Nullable Player player, @NonNull String world, @Nullable BlockTransaction transaction, @Nullable BlockSnapshot blockSnapshot) throws SQLException {
        String playerUUID;

        if (player == null) { // Server
            playerUUID = "0";

            insertAction.setDouble(9, 0);
            insertAction.setDouble(10, 0);
            insertAction.setDouble(11, 0);

            insertAction.setDouble(12, 0);
            insertAction.setDouble(13, 0);
            insertAction.setDouble(14, 0);

            insertAction.setString(15, "none");
        } else { // Player
            playerUUID = player.profile().uuid().toString();

            insertAction.setDouble(9, player.position().x());
            insertAction.setDouble(10, player.position().y());
            insertAction.setDouble(11, player.position().z());

            insertAction.setDouble(12, player.rotation().x()); // pitch
            insertAction.setDouble(13, player.rotation().y()); // yaw
            insertAction.setDouble(14, player.rotation().z()); // roll

            insertAction.setString(15, player.itemInHand(HandTypes.MAIN_HAND).type().key(RegistryTypes.ITEM_TYPE).formatted());
        }
        insertAction.setInt(1, type.id());
        insertAction.setTimestamp(2, new Timestamp(Instant.now().getEpochSecond()));
        insertAction.setString(3, playerUUID);

        String blockId;
        int x = 0;
        int y = -4096;
        int z = 0;

        if (transaction != null) {
            if (type.equals(ActionType.PLACE)) {
                blockId = transaction.defaultReplacement().state().type().key(RegistryTypes.BLOCK_TYPE).formatted();
            } else {
                blockId = transaction.original().state().type().key(RegistryTypes.BLOCK_TYPE).formatted();
            }

            // balls
            if (transaction.original().location().get() != null) {
                x = transaction.original().location().get().blockX();
                y = transaction.original().location().get().blockY();
                z = transaction.original().location().get().blockZ();
            }
        } else if (blockSnapshot != null) {
            if (!blockSnapshot.location().isPresent()) {
                throw new RuntimeException("Block location was not present (?!)");
            }
            ServerLocation serverLocation = blockSnapshot.location().get();
            x = serverLocation.blockX();
            y = serverLocation.blockY();
            z = serverLocation.blockZ();

            blockId = blockSnapshot.state().type().key(RegistryTypes.BLOCK_TYPE).formatted();
        } else {
            throw new RuntimeException("You must provide either a BlockTransaction or a BlockSnapshot");
        }

        if (y == -4096) {
            throw new RuntimeException("Could not locate block position!");
        }

        insertAction.setInt(6, x);
        insertAction.setInt(7, y);
        insertAction.setInt(8, z);

        insertAction.setString(4, blockId);
        insertAction.setString(5, world);

        insertAction.execute();
    }

    public ArrayList<StoredBlock> queryBlock(String world, int x, int y, int z) throws SQLException {
        queryCoords.setString(1, world);
        queryCoords.setInt(2, x);
        queryCoords.setInt(3, y);
        queryCoords.setInt(4, z);

        ResultSet rs = queryCoords.executeQuery();

        ArrayList blocks = new ArrayList<StoredBlock>();

        while (rs.next()) {
            blocks.add(this.blockFromRs(rs));
        }

        Collections.reverse(blocks); // Reverse order so we have newest first
        return blocks;

    }

    public StoredBlock queryId(int id) throws SQLException {
        queryId.setInt(1, id);

        ResultSet rs = queryId.executeQuery();

        if (rs.next()) {
            return this.blockFromRs(rs);
        }
        return null;
    }

    public ArrayList<StoredBlock> queryRange(int startX, int startY, int startZ, int endX, int endY, int endZ) throws SQLException {
        // Rearrange from smallest to biggest for things to actually get picked up
        int[] x = {startX, endX};
        int[] y = {startY, endY};
        int[] z = {startZ, endZ};

        // The smallest number would be at the start.
        Arrays.sort(x);
        Arrays.sort(y);
        Arrays.sort(z);

        // SELECT * FROM blocklog WHERE x BETWEEN ? and ? AND y BETWEEN ? and ? AND z BETWEEN ? AND ?

        queryRange.setInt(1, x[0]);
        queryRange.setInt(2, x[1]);

        queryRange.setInt(3, y[0]);
        queryRange.setInt(4, y[1]);

        queryRange.setInt(5, z[0]);
        queryRange.setInt(6, z[1]);

        ResultSet rs = queryRange.executeQuery();

        ArrayList<StoredBlock> blocks = new ArrayList<>();

        while (rs.next()) {
            blocks.add(this.blockFromRs(rs));
        }

        Collections.reverse(blocks); // Reverse order so we have newest first
        return blocks;
    }

    private StoredBlock blockFromRs(ResultSet rs) throws SQLException {
        int uid = rs.getInt("id");
        int type = rs.getInt("type");
        Timestamp timestamp = rs.getTimestamp("time");
        String playerUUID = rs.getString("player_uuid");
        String blockId = rs.getString("block_id");
        String world = rs.getString("world");
        String itemInHand = rs.getString("player_tool");

        int x = rs.getInt("x");
        int y = rs.getInt("y");
        int z = rs.getInt("z");

        double playerX = 0;
        double playerY = 0;
        double playerZ = 0;

        double playerPitch = 0;
        double playerYaw = 0;
        double playerRoll = 0;

        if (!playerUUID.equals("0")) {
            playerX = rs.getDouble("player_x");
            playerY = rs.getDouble("player_y");
            playerZ = rs.getDouble("player_z");

            playerPitch = rs.getDouble("player_pitch");
            playerYaw = rs.getDouble("player_yaw");
            playerRoll = rs.getDouble("player_roll");
        }

        double finalPlayerX = playerX;
        double finalPlayerY = playerY;
        double finalPlayerZ = playerZ;

        double finalPlayerRoll = playerRoll;
        double finalPlayerYaw = playerYaw;
        double finalPlayerPitch = playerPitch;

        return (new StoredBlock() { // TODO: perhaps this should be done with an impl in a sperate class.
            @Override
            public int uid() {
                return uid;
            }

            @Override
            public Optional<User> user() {
                if (playerUUID.equals("0")) {
                    return Optional.empty();
                }

                try {
                    return Sponge.server().userManager().load(UUID.fromString(playerUUID)).get();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Timestamp time() {
                return timestamp;
            }

            @Override
            public ActionType actionType() {
                return ActionType.fromId(type);
            }

            @Override
            public String blockId() {
                return blockId;
            }

            @Override
            public String world() {
                return world;
            }

            @Override
            public Vector3d playerLocation() {
                return new Vector3d(finalPlayerX, finalPlayerY, finalPlayerZ);
            }

            @Override
            public Vector3d playerRotation() {
                return new Vector3d(finalPlayerPitch, finalPlayerYaw, finalPlayerRoll);
            }

            @Override
            public String itemInHand() {
                return itemInHand;
            }

            @Override
            public int x() {
                return x;
            }

            @Override
            public int y() {
                return y;
            }

            @Override
            public int z() {
                return z;
            }
        });
    }

}
