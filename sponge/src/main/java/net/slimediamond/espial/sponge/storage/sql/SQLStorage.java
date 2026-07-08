package net.slimediamond.espial.sponge.storage.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import net.slimediamond.espial.api.storage.EspialStorageException;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.storage.DefaultStorage;
import net.slimediamond.espial.sponge.storage.RecordStorage;
import net.slimediamond.espial.sponge.storage.Result;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SQLStorage extends DefaultStorage {

    private final Map<String, Integer> idCache = new HashMap<>();
    private final String connectionString;
    private DataSource dataSource;
    private boolean sqlite;

    public SQLStorage(final String connectionString) {
        this.connectionString = connectionString;

        final SQLBlockStorage blockStorage = new SQLBlockStorage(this);
        final SQLContainerStorage containerStorage = new SQLContainerStorage(this);
        final SQLItemFrameStorage itemFrameStorage = new SQLItemFrameStorage(this);
        final SQLHangingDeathStorage hangingDeathStorage = new SQLHangingDeathStorage(this);
        final SQLSignModifyStorage signModifyStorage = new SQLSignModifyStorage(this);

        this.addSubmitter(EspialEvents.BREAK.location(), blockStorage);
        this.addSubmitter(EspialEvents.PLACE.location(), blockStorage);
        this.addSubmitter(EspialEvents.MODIFY.location(), blockStorage);
        this.addSubmitter(EspialEvents.GROWTH.location(), blockStorage);
        this.addSubmitter(EspialEvents.ITEM_INSERT.location(), containerStorage);
        this.addSubmitter(EspialEvents.ITEM_REMOVE.location(), containerStorage);
        this.addSubmitter(EspialEvents.ITEM_FRAME_INSERT.location(), itemFrameStorage);
        this.addSubmitter(EspialEvents.ITEM_FRAME_REMOVE.location(), itemFrameStorage);
        this.addSubmitter(EspialEvents.HANGING_DEATH.location(), hangingDeathStorage);
        this.addSubmitter(EspialEvents.SIGN_MODIFY.location(), signModifyStorage);

        this.addQuerier(blockStorage);
        this.addQuerier(containerStorage);
        this.addQuerier(itemFrameStorage);
        this.addQuerier(hangingDeathStorage);
        this.addQuerier(signModifyStorage);
    }

    public Connection getConn() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Opens and sets up the database
     *
     * @throws EspialStorageException If opening the database fails
     */
    public void open() throws EspialStorageException {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(connectionString);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(Espial.getInstance().getConfig().getDatabasePoolSize());
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setValidationTimeout(5000); // 5 seconds
        config.setKeepaliveTime(300000); // 5 mins
        config.setConnectionTestQuery("SELECT 1");
        dataSource = new HikariDataSource(config);

        try (final Connection conn = getConn()) {
            // TODO: TEMPORARILY DISABLED
            // TODO: PUT NEW CREATION STATEMENTS HERE
//            final String recordsCreation;
//            final String blockStatesCreation;
//            final String entityTypesCreation;
//            final String worldsCreation;
//            final String signsCreation;
//            final String itemsCreation;
//
//            final String extraCreation = "CREATE TABLE IF NOT EXISTS extra (" +
//                    "record_id INT NOT NULL, " +
//                    "original TEXT, " +
//                    "replacement TEXT, " +
//                    "FOREIGN KEY (record_id) REFERENCES records(id) ON DELETE CASCADE" +
//                    ")";
//
//            final String blockStateCreation = "CREATE TABLE IF NOT EXISTS block_state (" +
//                    "record_id INT NOT NULL, " +
//                    "original INT NOT NULL, " +
//                    "replacement INT NOT NULL, " +
//                    "FOREIGN KEY (record_id) REFERENCES records(id) ON DELETE CASCADE, " +
//                    "FOREIGN KEY (original) REFERENCES block_states(id), " +
//                    "FOREIGN KEY (replacement) REFERENCES block_states(id)" +
//                    ")";
//
//            final String chestItemCreation = "CREATE TABLE IF NOT EXISTS chest_item (" +
//                    "record_id INT NOT NULL, " +
//                    "original INT NOT NULL, " +
//                    "replacement INT NOT NULL, " +
//                    "slot INT NOT NULL, " +
//                    "FOREIGN KEY (record_id) REFERENCES records(id) ON DELETE CASCADE, " +
//                    "FOREIGN KEY (original) REFERENCES items(id), " +
//                    "FOREIGN KEY (replacement) REFERENCES items(id)" +
//                    ")";
//
//            final String itemFrameCreation = "CREATE TABLE IF NOT EXISTS item_frame (" +
//                    "record_id INT NOT NULL, " +
//                    "item INT NOT NULL, " +
//                    "FOREIGN KEY (record_id) REFERENCES records(id) ON DELETE CASCADE, " +
//                    "FOREIGN KEY (item) REFERENCES items(id)" +
//                    ")";
//
//            final String signCreation = "CREATE TABLE IF NOT EXISTS sign (" +
//                    "record_id INT NOT NULL, " +
//                    "original INT NOT NULL, " +
//                    "replacement INT NOT NULL, " +
//                    "FOREIGN KEY (record_id) REFERENCES records(id) ON DELETE CASCADE, " +
//                    "FOREIGN KEY (original) REFERENCES signs(id), " +
//                    "FOREIGN KEY (replacement) REFERENCES signs(id)" +
//                    ")";
//
//            // Databases need to be made differently on different databases.
//            this.sqlite = connectionString.contains("sqlite");
//            if (sqlite) {
//                Espial.getInstance().getLogger().info("Detected database type: sqlite");
//
//                recordsCreation = "CREATE TABLE IF NOT EXISTS records " +
//                        "(id INTEGER PRIMARY KEY AUTOINCREMENT, ";
//                blockStatesCreation = "CREATE TABLE IF NOT EXISTS block_states (" +
//                        "id INTEGER PRIMARY KEY AUTOINCREMENT, ";
//                entityTypesCreation = "CREATE TABLE IF NOT EXISTS entity_types (" +
//                        "id INTEGER PRIMARY KEY AUTOINCREMENT, ";
//                worldsCreation = "CREATE TABLE IF NOT EXISTS worlds (" +
//                        "id INTEGER PRIMARY KEY AUTOINCREMENT, ";
//                signsCreation = "CREATE TABLE IF NOT EXISTS signs (" +
//                        "id INTEGER PRIMARY KEY AUTOINCREMENT, ";
//                itemsCreation = "CREATE TABLE IF NOT EXISTS items (" +
//                        "id INTEGER PRIMARY KEY AUTOINCREMENT, ";
//            } else {
//                // Probably MySQL/MariaDB or whatever. use a different statement
//
//                Espial.getInstance().getLogger().info("Detected database type: MySQL/MariaDB");
//
//                recordsCreation = "CREATE TABLE IF NOT EXISTS records (" +
//                        "id INT AUTO_INCREMENT PRIMARY KEY, ";
//                blockStatesCreation = "CREATE TABLE IF NOT EXISTS block_states (" +
//                        "id INT AUTO_INCREMENT PRIMARY KEY, ";
//                entityTypesCreation = "CREATE TABLE IF NOT EXISTS entity_types (" +
//                        "id INT AUTO_INCREMENT PRIMARY KEY, ";
//                worldsCreation = "CREATE TABLE IF NOT EXISTS worlds (" +
//                        "id INT AUTO_INCREMENT PRIMARY KEY, ";
//                signsCreation = "CREATE TABLE IF NOT EXISTS signs (" +
//                        "id INT AUTO_INCREMENT PRIMARY KEY, ";
//                itemsCreation = "CREATE TABLE IF NOT EXISTS items (" +
//                        "id INT AUTO_INCREMENT PRIMARY KEY, ";
//            }
//
//            // order matters! Be careful.
//            conn.prepareStatement(entityTypesCreation + "resource_key TEXT NOT NULL)").execute();
//            conn.prepareStatement(worldsCreation + "resource_key TEXT NOT NULL)").execute();
//            conn.prepareStatement(blockStatesCreation + "state TINYTEXT NOT NULL)").execute();
//            conn.prepareStatement(signsCreation +
//                    "front_1 VARCHAR(384), " +
//                    "front_2 VARCHAR(384), " +
//                    "front_3 VARCHAR(384), " +
//                    "front_4 VARCHAR(384), " +
//                    "back_1 VARCHAR(384), " +
//                    "back_2 VARCHAR(384), " +
//                    "back_3 VARCHAR(384), " +
//                    "back_4 VARCHAR(384))").execute();
//            conn.prepareStatement(recordsCreation +
//                    "type TINYINT NOT NULL, " +
//                    "time TIMESTAMP NOT NULL, " +
//                    "player_uuid CHAR(36), " +
//                    "entity_type INT NOT NULL, " +
//                    "target VARCHAR(255) NOT NULL, " +
//                    "world INT NOT NULL, " +
//                    "x INT NOT NULL, " +
//                    "y INT NOT NULL, " +
//                    "z INT NOT NULL, " +
//                    "rolled_back BOOLEAN NOT NULL DEFAULT FALSE, " +
//                    "FOREIGN KEY (entity_type) REFERENCES entity_types(id), " +
//                    "FOREIGN KEY (world) REFERENCES worlds(id)" +
//                    ")").execute();
//            conn.prepareStatement(itemsCreation + "data TEXT NOT NULL)").execute();
//            conn.prepareStatement(chestItemCreation).execute();
//            conn.prepareStatement(itemFrameCreation).execute();
//            conn.prepareStatement(blockStateCreation).execute();
//            conn.prepareStatement(signCreation).execute();
//            conn.prepareStatement(extraCreation).execute();
//
//            if (sqlite) {
//                conn.prepareStatement("PRAGMA foreign_keys = ON").execute();
//                // prevent database file locking on sqlite
//                //conn.prepareStatement("PRAGMA journal_mode = WAL").execute();
//            }

            // make a base view which we can conveniently query all of the base information from
            conn.prepareStatement("""
                CREATE OR REPLACE VIEW records_view AS SELECT
                    records.id, records.time, records.rolled_back, players.uuid AS player_uuid,
                    location.x AS x, location.y AS y, location.z AS z, event_types.event_type
                    AS type, world.resource_key AS world_key, entity_types.resource_key AS entity_type_key
                    FROM records
                    LEFT JOIN players ON players.id = records.player
                    LEFT JOIN locations AS location ON location.id = records.location
                    LEFT JOIN worlds AS world ON location.world_id = world.id
                    LEFT JOIN event_types ON event_types.id = records.event_type
                    LEFT JOIN entity_types ON records.entity_type = entity_types.id
            """).execute();
        } catch (final SQLException e) {
            throw new EspialStorageException("Could not open the database", e, Collections.emptyList());
        }
    }

    public int getOrCreateId(final Connection conn, final String table, final Map<String, Object> data, int type) throws SQLException {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("Provided column-data map must not be empty");
        }

        // Build a key for the cache map to actually use
        final String key = table + "|" + data.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("|"));

        // Integer because it might be null
        final Integer cached = idCache.get(key);
        if (cached != null) {
            return cached;
        }

        // DB lookup
        final String whereClause = data.keySet().stream()
                .map(k -> k + " = ?")
                .collect(Collectors.joining(" AND "));
        final PreparedStatement select = conn.prepareStatement("SELECT id FROM " + table + " WHERE " + whereClause);
        int i = 1;
        for (final Object value : data.values()) {
            select.setObject(i++, value, type);
        }
        final ResultSet rs = select.executeQuery();
        if (rs.next()) {
            int id = rs.getInt(1);
            idCache.put(key, id);
            return id;
        }

        // nothing in the db so insert it

        final String columns = String.join(", ", data.keySet());
        final String placeholders = data.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        final PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")", Statement.RETURN_GENERATED_KEYS);
        i = 1;
        for (final Object value : data.values()) {
            insert.setObject(i++, value, type);
        }
        insert.executeUpdate();

        final ResultSet keys = sqlite
                ? conn.prepareStatement("SELECT last_insert_rowid()").executeQuery()
                : insert.getGeneratedKeys();
        keys.next();
        int id = keys.getInt(1);

        idCache.put(key, id);
        return id;
    }

    public int getOrCreateId(final Connection conn, final String table, final String column, final String data) throws SQLException {
        return this.getOrCreateId(conn, table, Map.of(column, data), Types.VARCHAR);
    }

    @Override
    public void setRolledBack(EspialRecord record, boolean rolledBack) {

    }

    /**
     * Return a {@link ResultSet} based on the given connection, sql, and {@link EspialQuery}
     * parameters
     * 
     * @param conn The connection to use to query the database. This may be
     *             obtained with {@link #getConn()}
     * @param sql The leading SQL query to fill in the blanks and return
     *            results for
     * @param query The Espial query with which to add {@code WHERE} or {@code AND}
     *              clauses accordingly.
     * @param storage The record type storage used for the query
     * @return A {@link ResultSet} based on the given input queries
     * @throws SQLException If the database throws an error, i.e. the given SQL is invalid
     * @see net.slimediamond.espial.api.storage.EspialStorage#query(EspialQuery) 
     */
    public ResultSet query(final Connection conn, final String sql, final EspialQuery query, final RecordStorage<?> storage) throws SQLException {
        final StringBuilder builder = new StringBuilder(
                sql +
                        // the below checks for whether we already have a WHERE clause (and thus need to
                        // use AND instead), it's a bit scuffed, but it works
                        (sql.toLowerCase().contains("where") ? "AND" : "WHERE") + " world_key = ? " + // 1
                        "AND x BETWEEN ? AND ? " + // 2, 3
                        "AND y BETWEEN ? AND ? " + // 4, 5
                        "AND z BETWEEN ? AND ? "   // 6, 7
        );

        query.getAfter().ifPresent(after -> builder.append(" AND time > ?"));   // 8
        query.getBefore().ifPresent(before -> builder.append(" AND time < ?")); // 9

        // Sponge handles input validation for us, so no sql injection :)

        final List<UUID> uuids = query.getUsers();
        if (uuids != null && !uuids.isEmpty()) {
            final List<String> players = new LinkedList<>();
            uuids.forEach(uuid -> players.add("\"" + uuid.toString() + "\""));
            builder.append(" AND player_uuid IN (").append(String.join(", ", players)).append(")");
        }
        // TODO: we can filter slightly nicer for block *states* rather than types at some point
        if (!query.getBlockTypes().isEmpty()) {
            final List<String> quoted = new LinkedList<>();
            query.getBlockTypes().forEach(block -> quoted.add("\"" + block.key(RegistryTypes.BLOCK_TYPE).formatted() + "\""));
            builder.append(" AND target IN (").append(String.join(", ", quoted)).append(")");
        }

        // event types stuff
        final Set<EspialEvent> events = storage.getHandledEvents();
        query.getEvents().forEach(events::remove); // remove anything the player doesn't want

        if (!events.isEmpty()) {
            builder.append("AND type IN (");
            for (int i = 0; i < events.size(); i++) {
                if (i == 0) {
                    builder.append("?");
                } else {
                    builder.append(", ?");
                }
            }
            builder.append(")");
        }

        final PreparedStatement ps = conn.prepareStatement(builder.toString());
        ps.setString(1, query.getWorldKey().formatted());

        // Ranged lookup

        // Rearrange from smallest to biggest for things to actually get picked up
        int[] x = {query.getMinimumPosition().x(), query.getMaximumPosition().x()};
        int[] y = {query.getMinimumPosition().y(), query.getMaximumPosition().y()};
        int[] z = {query.getMinimumPosition().z(), query.getMaximumPosition().z()};

        // The smallest number would be at the start.
        Arrays.sort(x);
        Arrays.sort(y);
        Arrays.sort(z);

        ps.setInt(2, x[0]);
        ps.setInt(3, x[1]);

        ps.setInt(4, y[0]);
        ps.setInt(5, y[1]);

        ps.setInt(6, z[0]);
        ps.setInt(7, z[1]);

        final AtomicInteger index = new AtomicInteger(7);

        if (query.getAfter().isPresent()) {
            final Date after = query.getAfter().get();
            final Timestamp timestamp = new Timestamp(after.getTime());
            ps.setTimestamp(index.incrementAndGet(), timestamp);
        }
        if (query.getBefore().isPresent()) {
            final Date before = query.getBefore().get();
            final Timestamp timestamp = new Timestamp(before.getTime());
            ps.setTimestamp(index.incrementAndGet(), timestamp);
        }

        for (final EspialEvent event : events) {
            ps.setString(index.incrementAndGet(),
                    Sponge.game().registry(EspialRegistryTypes.EVENT).valueKey(event).formatted());
        }

        return ps.executeQuery();
    }

    /**
     * Insert a record into the database and return its generated ID
     *
     * @param conn The connection with which to insert
     * @param record The record to insert
     * @return The ID generated by the storage backend
     * @throws SQLException If the database errors
     */
    public int insert(final Connection conn, final EspialRecord record) throws SQLException {
        final PreparedStatement ps = conn.prepareStatement("INSERT INTO records " +
                        "(event_type," +
                        "time," +
                        "player," +
                        "entity_type," +
                        "location) " +
                        " VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);

        final Timestamp timestamp = new Timestamp(record.getDate().getTime());
        ps.setTimestamp(2, timestamp);
        if (record.getUser().isPresent()) {
            ps.setInt(3, this.getOrCreateId(conn, "players", "uuid",
                    record.getUser().get().toString()));
        } else {
            ps.setNull(3, Types.INTEGER);
        }

        // this makes stuff slower, but it's much more efficient for storage
        final int recordTypeId = this.getOrCreateId(conn, "event_types", "event_type",
                record.getEvent().key(EspialRegistryTypes.EVENT).formatted());
        final int entityTypeId = this.getOrCreateId(conn, "entity_types", "resource_key",
                record.getEntityType().key(RegistryTypes.ENTITY_TYPE).formatted());
        final int worldId = this.getOrCreateId(conn, "worlds", "resource_key",
                record.getLocation().worldKey().formatted());
        final int locationId = this.getOrCreateId(conn, "locations",
                Map.of(
                        "world_id", worldId,
                        "x", record.getLocation().blockX(),
                        "y", record.getLocation().blockY(),
                        "z", record.getLocation().blockZ()
                ),
                Types.INTEGER);

        ps.setInt(1, recordTypeId);
        ps.setInt(4, entityTypeId);
        ps.setInt(5, locationId);

        ps.execute();

        ResultSet rs;
        try {
            rs = ps.getGeneratedKeys();
        } catch (final SQLFeatureNotSupportedException e) {
            // Try to use another approach (this should work for sqlite)
            rs = conn.prepareStatement("SELECT last_insert_rowid()").executeQuery();
        }
        if (rs.next()) {
            return rs.getInt(1);
        }
        return -1;
    }

    public static Result buildRecord(final ResultSet rs) throws SQLException {
        final String type = rs.getString("type");
        final EspialEvent event = EspialEvents.registry().streamEntries()
                .map(RegistryEntry::value)
                .filter(e -> e.key(EspialRegistryTypes.EVENT).formatted().equals(type))
                .findFirst().orElseThrow(() ->
                        new IllegalStateException("No Espial event associated with event ID '" + type + "'"));

        final int id = rs.getInt("id");
        final Date date = Date.from(rs.getTimestamp("time").toInstant());

        UUID user = null;
        String playerUuid = rs.getString("player_uuid");
        if (playerUuid != null) {
            user = UUID.fromString(playerUuid);
        }

        final EntityType<?> entityType = EntityTypes.registry()
                .value(ResourceKey.resolve(rs.getString("entity_type_key")));
        final ResourceKey worldKey = ResourceKey.resolve(rs.getString("world_key"));
        final int x = rs.getInt("x");
        final int y = rs.getInt("y");
        final int z = rs.getInt("z");
        final ServerLocation location = ServerLocation.of(worldKey, Vector3i.from(x, y, z));
        final Date rolledBack = rs.getDate("rolled_back");

        return new Result(type, event, id, date, user, entityType, worldKey, x, y, z, location, rolledBack);
    }

    public PreparedStatement prepareExtraDataStatement(final Connection conn) throws SQLException {
        return conn.prepareStatement("INSERT INTO extra (record_id, original_data, replacement_data) " +
                "VALUES (?, ?, ?)");
    }

    @Override
    public void batchSetRolledBack(List<EspialRecord> records, boolean rolledBack) throws EspialStorageException {
        if (records.isEmpty()) {
            return;
        }
        try (final Connection conn = getConn()) {
            final String placeholders = String.join(", ", Collections.nCopies(records.size(), "?"));
            final PreparedStatement ps = conn.prepareStatement(
                    "UPDATE records SET rolled_back = ? WHERE id IN (" + placeholders + ")"
            );
            if (rolledBack) {
                ps.setDate(1, new java.sql.Date(System.currentTimeMillis()));
            } else {
                ps.setNull(1, Types.DATE);
            }
            for (int i = 0; i < records.size(); i++) {
                ps.setInt(i + 2, records.get(i).getId());
            }
            ps.executeUpdate();
        } catch (final SQLException e) {
            throw new EspialStorageException(e);
        }
    }

    @Override
    public void batchDelete(List<Integer> ids) throws EspialStorageException {
        if (ids.isEmpty()) {
            return;
        }
        try (final Connection conn = getConn()) {
            final String placeholders = String.join(", ", Collections.nCopies(ids.size(), "?"));
            final PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM records WHERE id IN (" + placeholders + ")"
            );
            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            ps.executeUpdate();
        } catch (final SQLException e) {
            throw new EspialStorageException(e);
        }
    }

}
