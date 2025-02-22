package net.slimediamond.espial;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.slimediamond.espial.api.action.*;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.api.nbt.json.JsonNBTData;
import net.slimediamond.espial.api.nbt.NBTData;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.EntityRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import net.slimediamond.espial.api.user.EspialActor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.math.vector.Vector3d;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Database {
    boolean logPlayerPosition;
    private Connection conn;

    private PreparedStatement insertAction;
    private PreparedStatement queryCoords;
    private PreparedStatement queryId;
    private PreparedStatement queryRange;
    private PreparedStatement getBlockOwner;
    private PreparedStatement setRolledBack;
    private PreparedStatement insertNBTdata;
    private PreparedStatement getNBTdata;

    public Database() {
        this.logPlayerPosition = Espial.getInstance().getConfig().get().logPlayerPosition();
    }

    public void open(String connectionString) throws SQLException {
        conn = DriverManager.getConnection(connectionString);

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
            "player_tool TINYTEXT, " +
            "rolled_back BOOLEAN NOT NULL DEFAULT FALSE" +
            ")"
        ).execute();

        // Create the nbt table
        if (connectionString.contains("sqlite")) {
            sql = "CREATE TABLE IF NOT EXISTS nbt (" +
                    "id INTEGER PRIMARY KEY, " +
                    "data TEXT" +
                    ")";
        } else {
            sql = "CREATE TABLE IF NOT EXISTS nbt (" +
                    "id INT PRIMARY KEY, " +
                    "data TEXT" +
                    ")";
        }

        conn.prepareStatement(sql).execute();

        // Backwards compatibility
        //conn.prepareStatement("ALTER TABLE blocklog ADD COLUMN IF NOT EXISTS rolled_back BOOLEAN DEFAULT FALSE").execute();

        insertAction = conn.prepareStatement("INSERT INTO blocklog " +
                "(type, " +
                "time, " +
                "player_uuid, " +
                "block_id, " +
                "world, " +
                "x, y, z, " +
                "player_x, " +
                "player_y, " +
                "player_z, " +
                "player_pitch, " +
                "player_yaw, " +
                "player_roll, " +
                "player_tool, " +
                "rolled_back" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE)", Statement.RETURN_GENERATED_KEYS);

        queryCoords = conn.prepareStatement("SELECT * FROM blocklog WHERE world = ? AND x = ? AND y = ? AND z = ? AND player_uuid = COALESCE(?, player_uuid) AND block_id = COALESCE(?, block_id) AND time > COALESCE(?, time)");
        queryId = conn.prepareStatement("SELECT * FROM blocklog WHERE id = ?");
        queryRange = conn.prepareStatement("SELECT * FROM blocklog WHERE world = ? AND x BETWEEN ? and ? AND y BETWEEN ? and ? AND z BETWEEN ? AND ? AND player_uuid = COALESCE(?, player_uuid) AND block_id = COALESCE(?, block_id) AND time > COALESCE(?, time)");
        getBlockOwner = conn.prepareStatement("SELECT player_uuid FROM blocklog WHERE x = ? AND y = ? AND z = ? AND type = 1 ORDER BY time DESC LIMIT 1");
        setRolledBack = conn.prepareStatement("UPDATE blocklog SET rolled_back = ? WHERE id = ?");
        insertNBTdata = conn.prepareStatement("INSERT INTO nbt (id, data) VALUES (?, ?)");
        getNBTdata = conn.prepareStatement("SELECT data FROM nbt WHERE id = ?");
    }

    /**
     * Submit a {@link Action} to be inserted into the database.
     * @param action The action to submit
     * @return The record created
     * @throws SQLException If the database errors.
     * @throws JsonProcessingException If JSON errors.
     */
    public Optional<EspialRecord> submit(Action action) throws Exception {
        insertAction.setInt(1, action.getEventType().getId()); // Type
        insertAction.setTimestamp(2, Timestamp.from(Instant.now())); // Timestamp
        insertAction.setString(3, action.getActor().getUUID()); // Actor UUID

        // Block ID
        if (action instanceof BlockAction block) {
            insertAction.setString(4, block.getBlockId());
        } else if (action instanceof HangingDeathAction) {
            insertAction.setString(4, ((HangingDeathAction) action).getEntityType().key(RegistryTypes.ENTITY_TYPE).formatted());
        } else {
            insertAction.setString(4, ""); // apparently this can't be null?
        }

        insertAction.setString(5, action.getWorld()); // World
        insertAction.setInt(6, action.getX());
        insertAction.setInt(7, action.getY());
        insertAction.setInt(8, action.getZ());

        EspialActor actor = action.getActor();

        if (logPlayerPosition) {
            insertAction.setDouble(9,  actor.getPosition().x());
            insertAction.setDouble(10, actor.getPosition().y());
            insertAction.setDouble(11, actor.getPosition().z());

            insertAction.setDouble(12, actor.getRotation().x());
            insertAction.setDouble(13, actor.getRotation().y());
            insertAction.setDouble(14, actor.getRotation().z());
        } else {
            insertAction.setNull(9, Types.DOUBLE);
            insertAction.setNull(10, Types.DOUBLE);
            insertAction.setNull(11, Types.DOUBLE);

            insertAction.setNull(12, Types.DOUBLE);
            insertAction.setNull(13, Types.DOUBLE);
            insertAction.setNull(14, Types.DOUBLE);
        }

        insertAction.setString(15, actor.getItem());

        insertAction.execute();

        ResultSet rs;

        try {
            rs = insertAction.getGeneratedKeys();
        } catch (SQLFeatureNotSupportedException e) {
            // Try to use another approach (this should work for sqlite)
            rs = conn.prepareStatement("SELECT last_insert_rowid()").executeQuery();
        }

        if (rs.next()) {
            int id = rs.getInt(1);

            if (action instanceof NBTStorable nbtStorable) {
                if (nbtStorable.getNBT().isPresent()) {
                    this.setNBTdata(id, JsonNBTData.serialize(nbtStorable.getNBT().get()));
                }
            }

            return Optional.of(this.queryId(id));
        } else {
            return Optional.empty();
        }
    }

    public List<EspialRecord> query(Query query) throws Exception {
        Timestamp timestamp = query.getTimestamp() == null ? Timestamp.from(Instant.ofEpochMilli(0)) : query.getTimestamp();
        String uuid = query.getPlayerUUID() == null ? null : query.getPlayerUUID().toString();

        List<EspialRecord> actions = new ArrayList<>();
        ResultSet rs;

        if (query.getMax() == null) {
            // single. Query coords
            queryCoords.setString(1, query.getMin().worldKey().formatted());
            queryCoords.setInt(2, query.getMin().blockX());
            queryCoords.setInt(3, query.getMin().blockY());
            queryCoords.setInt(4, query.getMin().blockZ());
            queryCoords.setString(5, uuid);
            queryCoords.setString(6, query.getBlockId());
            queryCoords.setTimestamp(7, timestamp);

            rs = queryCoords.executeQuery();
        } else {
            // Ranged lookup

            // Rearrange from smallest to biggest for things to actually get picked up
            int[] x = {query.getMin().blockX(), query.getMax().blockX()};
            int[] y = {query.getMin().blockY(), query.getMax().blockY()};
            int[] z = {query.getMin().blockZ(), query.getMax().blockZ()};

            // The smallest number would be at the start.
            Arrays.sort(x);
            Arrays.sort(y);
            Arrays.sort(z);

            queryRange.setString(1, query.getMin().worldKey().formatted());

            queryRange.setInt(2, x[0]);
            queryRange.setInt(3, x[1]);

            queryRange.setInt(4, y[0]);
            queryRange.setInt(5, y[1]);

            queryRange.setInt(6, z[0]);
            queryRange.setInt(7, z[1]);

            queryRange.setString(8, uuid);
            queryRange.setString(9, query.getBlockId());
            queryRange.setTimestamp(10, timestamp);

            rs = queryRange.executeQuery();
        }

        while (rs.next()) {
            actions.add(blockFromRs(rs));
        }

        return actions;
    }

    public EspialRecord queryId(int id) throws Exception {
        queryId.setInt(1, id);

        ResultSet rs = queryId.executeQuery();

        if (rs.next()) {
            return this.blockFromRs(rs);
        }
        return null;
    }

    public Optional<User> getBlockOwner(int x, int y, int z) throws SQLException, ExecutionException, InterruptedException {
        getBlockOwner.setInt(1, x);
        getBlockOwner.setInt(2, y);
        getBlockOwner.setInt(3, z);

        ResultSet rs = getBlockOwner.executeQuery();
        if (rs.next()) {
            String playerUUID = rs.getString("player_uuid");
            try {
                UUID uuid = UUID.fromString(playerUUID);
                return Sponge.server().userManager().load(uuid).get();
            } catch (IllegalArgumentException e) {
                // likely to be an animal or something
                // TODO: Make some block owner object, then we can derive a name nicer
                return Optional.empty();
            }

        } else {
            return Optional.empty();
        }
    }

    private EspialRecord blockFromRs(ResultSet rs) throws Exception {
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


        boolean rolledBack = rs.getBoolean("rolled_back");

        EspialActor actor = new EspialActor() {
            @Override
            public String getUUID() {
                return playerUUID;
            }

            @Override
            public @Nullable Vector3d getPosition() {
                if (logPlayerPosition) {
                    return new Vector3d(finalPlayerX, finalPlayerY, finalPlayerZ);
                } else {
                    return null;
                }
            }

            @Override
            public @Nullable Vector3d getRotation() {
                if (logPlayerPosition) {
                    return new Vector3d(finalPlayerPitch, finalPlayerYaw, finalPlayerRoll);
                } else {
                    return null;
                }
            }

            @Override
            public String getItem() {
                return itemInHand;
            }
        };

        EventType eventType = EventTypes.fromId(type);

        Action action;

        if (eventType != null) {
            if (eventType.getActionType().equals(ActionType.BLOCK)) {
                action = BlockAction.builder()
                        .blockId(blockId)
                        .actor(actor)
                        .event(eventType)
                        .world(world)
                        .x(x)
                        .y(y)
                        .z(z)
                        .withNBTData(getNBTdata(uid).orElse(null))
                        .build();

                // Make a new BlockRecord
                return new BlockRecord(uid, timestamp, rolledBack, action);
            } if (eventType.getActionType().equals(ActionType.HANGING_DEATH)) {
                EntityType<?> entityType = EntityTypes.registry().value(ResourceKey.of(
                            blockId.split(String.valueOf(ResourceKey.DEFAULT_SEPARATOR))[0],
                            blockId.split(String.valueOf(ResourceKey.DEFAULT_SEPARATOR))[1]));

                action = HangingDeathAction.builder()
                        .actor(actor)
                        .entity(entityType)
                        .event(eventType)
                        .world(world)
                        .x(x)
                        .y(y)
                        .z(z)
                        .withNBTData(getNBTdata(uid).orElse(null))
                        .build();

                return new EntityRecord(uid, timestamp, rolledBack, action);
            } else {
                throw new Exception("Unsupported event type");
            }
        } else {
            throw new Exception("Event type cannot be null");
        }
    }

    public void setRolledBack(int id, boolean status) throws SQLException {
        setRolledBack.setBoolean(1, status);
        setRolledBack.setInt(2, id);
        setRolledBack.execute();
    }

    public void setNBTdata(int id, String data) throws SQLException {
        insertNBTdata.setInt(1, id);
        insertNBTdata.setString(2, data);
        insertNBTdata.execute();
    }

    public Optional<NBTData> getNBTdata(int id) throws SQLException, JsonProcessingException {
        getNBTdata.setInt(1, id);
        ResultSet rs = getNBTdata.executeQuery();

        if (rs.next()) {
            return Optional.of(JsonNBTData.deserialize(rs.getString("data")));
        }

        return Optional.empty();
    }
}
