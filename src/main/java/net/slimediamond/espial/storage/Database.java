package net.slimediamond.espial.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.action.ActionType;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.EntityAction;
import net.slimediamond.espial.api.action.HangingDeathAction;
import net.slimediamond.espial.api.action.ItemFrameRemoveAction;
import net.slimediamond.espial.api.action.NBTStorable;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.api.event.PostInsertRecordEvent;
import net.slimediamond.espial.api.event.PreInsertActionEvent;
import net.slimediamond.espial.api.nbt.NBTData;
import net.slimediamond.espial.api.nbt.json.JsonNBTData;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.Sort;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.user.EspialActor;
import net.slimediamond.espial.sponge.record.BlockRecordImpl;
import net.slimediamond.espial.sponge.record.EntityRecordImpl;
import net.slimediamond.espial.util.SpongeUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.registry.RegistryTypes;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Database {

    private DataSource dataSource;

    private PreparedStatement dropOldTable;

    private boolean hasLegacyTable;

    private Connection getConn() throws SQLException {
        return dataSource.getConnection();
    }

    public void open(String connectionString) throws SQLException {
        Espial.getInstance().getLogger().info("Opening database...");

        // Unsure if HikariCP is needed after June 6 2025, as it now gets
        // a new connection every time it needs one. Keeping it regardless
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(connectionString);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(5); // TODO config value?
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setValidationTimeout(5000); // 5 seconds
        config.setKeepaliveTime(300000); // 5 mins
        config.setConnectionTestQuery("SELECT 1");

        dataSource = new HikariDataSource(config);

        String creation;
        String legacyCheck;

        Connection conn = getConn();

        // Databases need to be made differently on different databases.
        if (connectionString.contains("sqlite")) {
            Espial.getInstance().getLogger().info("Detected database type: sqlite");
            creation = "CREATE TABLE IF NOT EXISTS records (id INTEGER PRIMARY KEY AUTOINCREMENT, ";
            legacyCheck = "SELECT name FROM sqlite_master WHERE type='table' AND name='blocklog'";
        } else {
            // Probably MySQL/MariaDB or whatever. use a different statement

            Espial.getInstance().getLogger().info("Detected database type: MySQL/MariaDB");

            creation = "CREATE TABLE IF NOT EXISTS records (id INT AUTO_INCREMENT PRIMARY KEY, ";
            legacyCheck = "SHOW TABLES LIKE 'blocklog'";
        }

        conn.prepareStatement(creation +
                "type TINYINT, " +
                "time TIMESTAMP, " +
                "player_uuid MEDIUMTEXT, " +
                "block_id TINYTEXT, " +
                "world TINYTEXT, " +
                "x INT, " +
                "y INT, " +
                "z INT, " +
                "rolled_back BOOLEAN NOT NULL DEFAULT FALSE" +
                ")").execute();

        hasLegacyTable = conn.prepareStatement(legacyCheck).executeQuery().next();

        // Migrate data from blocklog to records
        // Espial v1 --> v2
        //
        // Drops player_* values, because they took up space
        // and seemed useless.
        if (hasLegacyTable) {
            dropOldTable = getConn().prepareStatement("DROP TABLE blocklog");

            try {
                conn.prepareStatement(
                        "INSERT INTO records (type, time, player_uuid, block_id, world, x, y, z, rolled_back) " +
                                "SELECT type, time, player_uuid, block_id, world, x, y, z, rolled_back FROM blocklog").execute();
                Espial.getInstance().getLogger().info("Database migrated");
            } catch (SQLException ignored) { // Does not need to be migrated
            }
        }

        // Create the nbt table
        if (connectionString.contains("sqlite")) {
            creation = "CREATE TABLE IF NOT EXISTS nbt (id INTEGER PRIMARY KEY, data TEXT)";
        } else {
            creation = "CREATE TABLE IF NOT EXISTS nbt (id INT PRIMARY KEY, data TEXT)";
        }

        conn.prepareStatement(creation).execute();
        conn.close();

        Espial.getInstance().getLogger().info("Database loaded.");
    }

    /**
     * Submit a {@link Action} to be inserted into the database.
     *
     * @param action The action to submit
     * @return The record created
     * @throws SQLException            If the database errors.
     * @throws JsonProcessingException If JSON errors.
     */
    public Optional<EspialRecord> submit(Action action) throws Exception {
        Connection conn = getConn();
        Cause cause = Cause.builder().append(action.getActor()).build();
        Sponge.eventManager().post(new PreInsertActionEvent(action.getActor(), cause, action));

        Timestamp timestamp = Timestamp.from(Instant.now());

        PreparedStatement insertAction = conn.prepareStatement(
                "INSERT INTO records "
                        + "(type, "
                        + "time, "
                        + "player_uuid, "
                        + "block_id, "
                        + "world, "
                        + "x, y, z, "
                        + "rolled_back"
                        + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE)",
                Statement.RETURN_GENERATED_KEYS);

        insertAction.setInt(1, action.getEventType().getId()); // Type
        insertAction.setTimestamp(2, timestamp); // Timestamp
        insertAction.setString(3, action.getActor().getUUID()); // Actor UUID

        // Block ID
        switch (action) {
            case BlockAction block -> insertAction.setString(4, block.getBlockId());
            case HangingDeathAction hangingDeathAction -> insertAction.setString(4, hangingDeathAction.getEntityType().key(RegistryTypes.ENTITY_TYPE).formatted());
            case ItemFrameRemoveAction itemFrameRemoveAction -> insertAction.setString(4, itemFrameRemoveAction.getItemType().key(RegistryTypes.ITEM_TYPE).formatted());
            default -> throw new SQLException("Block id must be present. This action type is not recognized by the database.");
        }

        insertAction.setString(5, action.getWorld()); // World
        insertAction.setInt(6, action.getX());
        insertAction.setInt(7, action.getY());
        insertAction.setInt(8, action.getZ());

        EspialActor actor = action.getActor();

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

            // rolled back is false as we just submitted this
            EspialRecord record = switch (action) {
                case BlockAction blockAction -> new BlockRecordImpl(id, timestamp, false, blockAction);
                case EntityAction hangingDeathAction -> new EntityRecordImpl(id, timestamp, false, hangingDeathAction);
                default -> throw new SQLException("Invalid action type submitted to the database.");
            };

            Sponge.eventManager().post(new PostInsertRecordEvent(actor, cause, record));

            conn.close();
            return Optional.of(record);
        } else {
            // Should not happen
            conn.close();
            return Optional.empty();
        }
    }

    public List<EspialRecord> query(Query query) throws Exception {
        Timestamp timestamp = query.getTimestamp() == null
                        ? Timestamp.from(Instant.ofEpochMilli(0))
                        : query.getTimestamp();

        StringBuilder sql = new StringBuilder("SELECT * FROM records WHERE world" +
                " = ? AND time > ?");

        if (query.getMax() == null) {
            sql.append(" AND x = ? AND y = ? AND z = ? ");
        } else {
            sql.append(" AND x BETWEEN ? and ? AND y BETWEEN ? and ? AND z BETWEEN " +
                    "? and ? ");
        }

        List<UUID> uuids = query.getPlayerUUIDs();
        if (uuids != null && !uuids.isEmpty()) {
            List<String> players = new ArrayList<>();
            uuids.forEach(uuid -> players.add("\"" + uuid.toString() + "\""));
            sql.append(" AND player_uuid IN (").append(String.join(", ", players)).append(")");
        }

        List<String> blocks = query.getBlockIds();
        if (blocks != null && !blocks.isEmpty()) {
            List<String> quoted = new ArrayList<>();
            blocks.forEach(block -> quoted.add("\"" + block + "\""));
            sql.append(" AND block_id IN (").append(String.join(", ", quoted)).append(")");
        }

        List<EspialRecord> actions = new ArrayList<>();
        ResultSet rs;

        Connection conn = getConn();
        PreparedStatement statement = conn.prepareStatement(sql.toString());
        statement.setString(1, query.getMin().worldKey().formatted());
        statement.setTimestamp(2, timestamp);

        if (query.getMax() == null) {
            // single. Query coords
            statement.setInt(3, query.getMin().blockX());
            statement.setInt(4, query.getMin().blockY());
            statement.setInt(5, query.getMin().blockZ());

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


            statement.setInt(3, x[0]);
            statement.setInt(4, x[1]);

            statement.setInt(5, y[0]);
            statement.setInt(6, y[1]);

            statement.setInt(7, z[0]);
            statement.setInt(8, z[1]);

        }
        rs = statement.executeQuery();

        while (rs.next()) {
            actions.add(recordFromResultSet(rs));
        }

        conn.close();

        if (query.getSort() == Sort.ID_ASCENDING) {
            actions.sort(Comparator.comparing(EspialRecord::getId));
        } else if (query.getSort() == Sort.ID_DESCENDING) {
            actions.sort(Comparator.comparing(EspialRecord::getId).reversed());
        } else if (query.getSort() == Sort.REVERSE_CHRONOLOGICAL) {
            actions.sort(Comparator.comparing(EspialRecord::getTimestamp).reversed());
        } else if (query.getSort() == Sort.CHRONOLOGICAL) {
            actions.sort(Comparator.comparing(EspialRecord::getTimestamp));
        }

        return actions;
    }

    public EspialRecord queryId(int id) throws Exception {
        Connection conn = getConn();
        PreparedStatement queryId = conn.prepareStatement("SELECT * FROM records WHERE id = ?");
        queryId.setInt(1, id);

        ResultSet rs = queryId.executeQuery();

        if (rs.next()) {
            EspialRecord result = this.recordFromResultSet(rs);
            conn.close();
            return result;
        }
        conn.close();
        return null;
    }

    public Optional<User> getBlockOwner(String world, int x, int y, int z) throws SQLException, ExecutionException, InterruptedException {
        Connection conn = getConn();
        PreparedStatement getBlockOwner = conn.prepareStatement(
                "SELECT player_uuid FROM records " +
                "WHERE world = ? " +
                "AND x = ? " +
                "AND y = ? " +
                "AND z = ? " +
                "AND type = 1 " +
                "ORDER BY time DESC LIMIT 1"
        );
        getBlockOwner.setString(1, world);
        getBlockOwner.setInt(2, x);
        getBlockOwner.setInt(3, y);
        getBlockOwner.setInt(4, z);

        ResultSet rs = getBlockOwner.executeQuery();
        if (rs.next()) {
            String playerUUID = rs.getString("player_uuid");
            try {
                UUID uuid = UUID.fromString(playerUUID);
                conn.close();
                return Optional.of(Sponge.server().userManager().loadOrCreate(uuid).join());
            } catch (Exception e) {
                // likely to be an animal or something
                // TODO: Make some block owner object, then we can derive a name nicer
                conn.close();
                return Optional.empty();
            }

        } else {
            conn.close();
            return Optional.empty();
        }
    }

    private EspialRecord recordFromResultSet(ResultSet rs) throws Exception {
        int id = rs.getInt("id");
        int type = rs.getInt("type");
        Timestamp timestamp = rs.getTimestamp("time");
        String playerUUID = rs.getString("player_uuid");
        String blockId = rs.getString("block_id");
        String world = rs.getString("world");

        int x = rs.getInt("x");
        int y = rs.getInt("y");
        int z = rs.getInt("z");

        boolean rolledBack = rs.getBoolean("rolled_back");

        UUID uuid;
        try {
            uuid = UUID.fromString(playerUUID);
        } catch (IllegalArgumentException e) {
            uuid = null;
        }

        boolean isPlayer = uuid != null;

        EspialActor actor = new EspialActor() {
            @Override
            public String getUUID() {
                return playerUUID;
            }

            @Override
            public boolean isPlayer() {
                return isPlayer;
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
                        .withNBTData(getNBTdata(id).orElse(null))
                        .build();

                // Make a new BlockRecord
                return new BlockRecordImpl(id, timestamp, rolledBack, action);
            }
            if (eventType.getActionType().equals(ActionType.HANGING_DEATH)) {
                EntityType<?> entityType = EntityTypes.registry().value(SpongeUtil.getResourceKey(blockId));

                action = HangingDeathAction.builder()
                        .actor(actor)
                        .entity(entityType)
                        .event(eventType)
                        .world(world)
                        .x(x)
                        .y(y)
                        .z(z)
                        .withNBTData(getNBTdata(id).orElse(null))
                        .build();

                return new EntityRecordImpl(id, timestamp, rolledBack, action);
            } else if (eventType.getActionType().equals(ActionType.ITEM_FRAME_REMOVE)) {
                ItemType itemType = ItemTypes.registry().value(SpongeUtil.getResourceKey(blockId));

                action = ItemFrameRemoveAction.builder()
                        .itemType(itemType)
                        .actor(actor)
                        .event(eventType)
                        .world(world)
                        .x(x)
                        .y(y)
                        .z(z)
                        .build();

                return new EntityRecordImpl(id, timestamp, rolledBack, action);
            } else {
                throw new Exception("Unsupported event type");
            }
        } else {
            throw new Exception("Event type cannot be null");
        }
    }

    public void setRolledBack(int id, boolean status) throws SQLException {
        Connection conn = getConn();
        PreparedStatement setRolledBack = conn.prepareStatement("UPDATE records SET rolled_back = ? WHERE id = ?");

        setRolledBack.setBoolean(1, status);
        setRolledBack.setInt(2, id);
        setRolledBack.execute();
        conn.close();
    }

    public void setNBTdata(int id, String data) throws SQLException {
        Connection conn = getConn();
        PreparedStatement insertNBTdata = conn.prepareStatement("INSERT INTO nbt (id, data) VALUES (?, ?)");

        insertNBTdata.setInt(1, id);
        insertNBTdata.setString(2, data);
        insertNBTdata.execute();
        conn.close();
    }

    public Optional<NBTData> getNBTdata(int id) throws SQLException, JsonProcessingException {
        Connection conn = getConn();
        PreparedStatement getNBTdata = conn.prepareStatement("SELECT data FROM nbt WHERE id = ?");

        getNBTdata.setInt(1, id);
        ResultSet rs = getNBTdata.executeQuery();

        if (rs.next()) {
            JsonNBTData result = JsonNBTData.deserialize(rs.getString("data"));
            conn.close();
            return Optional.of(result);
        }

        conn.close();

        return Optional.empty();
    }

    /**
     * Drops the legacy <code>blocklog</code> database table.
     *
     * @return True if the table was dropped, false if it does not exist
     * @throws SQLException If the database errors
     */
    public boolean dropOldTable() throws SQLException {
        if (!hasLegacyTable || dropOldTable == null) {
            return false;
        }
        hasLegacyTable = false;
        dropOldTable.execute();
        return true;
    }

    /**
     * Gets whether the database currently has the legacy table
     *
     * @return Legacy table presence
     */
    public boolean hasLegacyTable() {
        return hasLegacyTable;
    }
}
