package net.slimediamond.espial.sponge.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.api.SignText;
import net.slimediamond.espial.api.event.InsertRecordEvent;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.*;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.event.SpongeInsertRecordEvent;
import net.slimediamond.espial.sponge.record.RecordFactoryProvider;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.registry.RegistryTypes;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class EspialDatabase {

    private final Map<String, Integer> idCache = new HashMap<>();
    private final String connectionString;
    private DataSource dataSource;
    private boolean sqlite;

    public EspialDatabase(@NotNull final String connectionString) {
        this.connectionString = connectionString;
    }

    private Connection getConn() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Opens and sets up the database
     *
     * @throws SQLException
     */
    public void open() throws SQLException {
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

            // TODO: Make a view for each type of record, and then query them simultaneously
            conn.prepareStatement("""
                CREATE OR REPLACE VIEW records_view AS
                    SELECT
                    records.id,
                    records.time,
                    records.rolled_back,
                    players.uuid as player_uuid,
                    location.x as x,
                    location.y as y,
                    location.z as z,
                    item_container.item_frame,
                    item_container.original_item,
                    item_container.replacement_item,
                    item_container.slot,
                    record_types.record_type as type,
                    world.resource_key as world_key,
                    extra.original_data as original_data,
                    extra.replacement_data as replacement_data,
                    original.state as original_state,
                    replacement.state as replacement_state,
                    entity_types.resource_key as entity_type_key,
                    signs_original.front_1 AS original_front_1,
                    signs_original.front_2 AS original_front_2,
                    signs_original.front_3 AS original_front_3,
                    signs_original.front_4 AS original_front_4,
                    signs_original.back_1  AS original_back_1,
                    signs_original.back_2  AS original_back_2,
                    signs_original.back_3  AS original_back_3,
                    signs_original.back_4  AS original_back_4,
                    signs_replacement.front_1 AS replacement_front_1,
                    signs_replacement.front_2 AS replacement_front_2,
                    signs_replacement.front_3 AS replacement_front_3,
                    signs_replacement.front_4 AS replacement_front_4,
                    signs_replacement.back_1  AS replacement_back_1,
                    signs_replacement.back_2  AS replacement_back_2,
                    signs_replacement.back_3  AS replacement_back_3,
                    signs_replacement.back_4  AS replacement_back_4,
                    item_original.data as item_original,
                    item_replacement.data as item_replacement
                    FROM records
                    LEFT JOIN players ON players.id = records.player
                    LEFT JOIN locations AS location ON location.id = records.location
                    LEFT JOIN worlds AS world ON location.world_id = world.id
                    LEFT JOIN sign_change ON records.id = sign_change.record_id
                    LEFT JOIN sign_text AS signs_original ON sign_change.original_text = signs_original.id
                    LEFT JOIN sign_text AS signs_replacement ON sign_change.replacement_text = signs_replacement.id
                    LEFT JOIN extra ON extra.record_id = records.id
                    LEFT JOIN record_types ON record_types.id = records.record_type
                    LEFT JOIN block_change AS bs ON records.id = bs.record_id
                    LEFT JOIN block_states AS original ON bs.original_block = original.id
                    LEFT JOIN block_states AS replacement ON bs.replacement_block = replacement.id
                    LEFT JOIN entity_types ON records.entity_type = entity_types.id
                    LEFT JOIN item_container ON item_container.record_id = records.id
                    LEFT JOIN items AS item_original ON item_original.id = item_container.original_item
                    LEFT JOIN items AS item_replacement ON item_replacement.id = item_container.replacement_item
            """);
        }
    }

    /**
     * Submit a {@link EspialRecord} to the database.
     *
     * @param record The record to insert
     * @return The ID primary key of the inserted record
     * @throws SQLException
     */
    public int submit(@NotNull final EspialRecord record) throws SQLException, IOException {
        try (final Connection conn = getConn()) {
            final PreparedStatement ps = conn.prepareStatement("INSERT INTO records " +
                            "(record_type," +
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
            final int recordTypeId = this.getOrCreateId(conn, "record_types", "record_type",
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
                final int id = rs.getInt(1);

                final DataQuery unsafeData = DataQuery.of("UnsafeData");
                final PreparedStatement insertExtra = conn.prepareStatement("INSERT INTO extra (record_id, original_data, replacement_data) " +
                        "VALUES (?, ?, ?)");

                if (record instanceof final BlockRecord blockRecord) {
                    // insert into block_state
                    final int originalState = getOrCreateId(conn, "block_states", "state",
                            blockRecord.getOriginalBlock().state().asString());
                    final int replacementState = getOrCreateId(conn, "block_states", "state",
                            blockRecord.getReplacementBlock().state().asString());

                    final PreparedStatement insertState = conn.prepareStatement("INSERT INTO block_change (record_id, original_block, replacement_block) " +
                            "VALUES (?, ?, ?)");

                    insertState.setInt(1, id);
                    insertState.setInt(2, originalState);
                    insertState.setInt(3, replacementState);
                    insertState.execute();

                    if (blockRecord.getOriginalBlock().toContainer().contains(unsafeData)
                            || blockRecord.getReplacementBlock().toContainer().contains(unsafeData)) {
                        insertExtra.setInt(1, id);
                        if (blockRecord.getOriginalBlock().toContainer().contains(unsafeData)) {
                            insertExtra.setString(2, DataFormats.JSON.get()
                                    .write(blockRecord.getOriginalBlock().toContainer()));
                        } else {
                            insertExtra.setNull(2, Types.CHAR);
                        }
                        if (blockRecord.getReplacementBlock().toContainer().contains(unsafeData)) {
                            insertExtra.setString(3, DataFormats.JSON.get()
                                    .write(blockRecord.getReplacementBlock().toContainer()));
                        } else {
                            insertExtra.setNull(3, Types.CHAR);
                        }
                        insertExtra.execute();
                    }
                } else if (record instanceof final HangingDeathRecord hangingDeathRecord) {
                    if (hangingDeathRecord.getExtraData().isPresent()
                            && hangingDeathRecord.getExtraData().get().contains(unsafeData)) {
                        insertExtra.setInt(1, id);
                        insertExtra.setString(2, DataFormats.JSON.get()
                                .write(hangingDeathRecord.getExtraData().get()));
                        insertExtra.setNull(3, Types.CHAR);
                        insertExtra.execute();
                    }
                } else if (record instanceof final SignModifyRecord signModifyRecord) {
                    // TODO: ideally each side is stored individually (as opposed to both sides)
                    // or heck even each line, but that's way too much effort :)
                    final SignText original = signModifyRecord.getOriginalContents();
                    final SignText replacement = signModifyRecord.getReplacementContents();

                    final Map<String, Object> originalSign = Map.of(
                            "front_1", componentToString(original.getFront1()),
                            "front_2", componentToString(original.getFront2()),
                            "front_3", componentToString(original.getFront3()),
                            "front_4", componentToString(original.getFront4()),

                            "back_1", componentToString(original.getBack1()),
                            "back_2", componentToString(original.getBack2()),
                            "back_3", componentToString(original.getBack3()),
                            "back_4", componentToString(original.getBack4())
                    );

                    final Map<String, Object> replacementSign = Map.of(
                            "front_1", componentToString(replacement.getFront1()),
                            "front_2", componentToString(replacement.getFront2()),
                            "front_3", componentToString(replacement.getFront3()),
                            "front_4", componentToString(replacement.getFront4()),

                            "back_1", componentToString(replacement.getBack1()),
                            "back_2", componentToString(replacement.getBack2()),
                            "back_3", componentToString(replacement.getBack3()),
                            "back_4", componentToString(replacement.getBack4())
                    );

                    final int originalId = getOrCreateId(conn, "sign_text", originalSign, Types.VARCHAR);
                    final int replacementId = getOrCreateId(conn, "sign_text", replacementSign, Types.VARCHAR);

                    final PreparedStatement insertSign = conn.prepareStatement("INSERT INTO sign_change (record_id, original_text, replacement_text) VALUES (?, ?, ?)");
                    insertSign.setInt(1, id);
                    insertSign.setInt(2, originalId);
                    insertSign.setInt(3, replacementId);
                    insertSign.execute();

                    final int state = getOrCreateId(conn, "block_states", "state",
                            signModifyRecord.getBlockState().asString());

                    final PreparedStatement insertState = conn.prepareStatement("INSERT INTO block_change (record_id, original_block, replacement_block) " +
                            "VALUES (?, ?, ?)");

                    insertState.setInt(1, id);
                    insertState.setInt(2, state);
                    insertState.setInt(3, state);
                    insertState.execute();
                } else if (record instanceof final ContainerChangeRecord containerChangeRecord) {
                    final int original = getOrCreateId(
                            conn,
                            "items",
                            "data",
                            DataFormats.JSON.get().write(containerChangeRecord.getOriginal().toContainer())
                    );
                    final int replacement = getOrCreateId(
                            conn,
                            "items",
                            "data",
                            DataFormats.JSON.get().write(containerChangeRecord.getReplacement().toContainer())
                    );
                    final PreparedStatement insertChestItem = conn.prepareStatement("INSERT INTO item_container (record_id, item_frame, original_item, replacement_item, slot) VALUES (?, ?, ?, ?, ?)");
                    insertChestItem.setInt(1, id);
                    insertChestItem.setBoolean(2, false);
                    insertChestItem.setInt(3, original);
                    insertChestItem.setInt(4, replacement);
                    insertChestItem.setInt(5, containerChangeRecord.getSlot());
                    insertChestItem.execute();
                } else if (record instanceof final ItemFrameChangeRecord itemFrameChangeRecord) {
                    final int item = getOrCreateId(
                            conn,
                            "items",
                            "data",
                            DataFormats.JSON.get().write(itemFrameChangeRecord.getAffectedItem().toContainer())
                    );
                    final PreparedStatement insertItem = conn.prepareStatement("INSERT INTO item_frame (record_id, item) VALUES (?, ?)");
                    insertItem.setInt(1, id);
                    insertItem.setInt(2, item);
                    insertItem.execute();
                }

                final Cause cause = Cause.of(EventContext.builder()
                        .add(EventContextKeys.PLUGIN, Espial.getInstance().getContainer())
                        .build(), this, record);
                final InsertRecordEvent.Post event = new SpongeInsertRecordEvent.PostImpl(record, cause);
                Sponge.eventManager().post(event);

                return id;
            }
            return -1;
        }
    }

    public List<EspialRecord> query(@NotNull final EspialQuery query) throws SQLException {
        final StringBuilder sql = new StringBuilder(
                "SELECT * FROM records_view " +
                "WHERE world_key = ? " +
                "AND x BETWEEN ? AND ? " +
                "AND y BETWEEN ? AND ? " +
                "AND z BETWEEN ? AND ? "
        );

        query.getAfter().ifPresent(after -> sql.append(" AND time > ?"));
        query.getBefore().ifPresent(before -> sql.append(" AND time < ?"));

        // Sponge handles input validation for us, so no sql injection :)

        final List<UUID> uuids = query.getUsers();
        if (uuids != null && !uuids.isEmpty()) {
            final List<String> players = new LinkedList<>();
            uuids.forEach(uuid -> players.add("\"" + uuid.toString() + "\""));
            sql.append(" AND player_uuid IN (").append(String.join(", ", players)).append(")");
        }
        // TODO: we can filter slightly nicer for block *states* rather than types at some point
        if (!query.getBlockTypes().isEmpty()) {
            final List<String> quoted = new LinkedList<>();
            query.getBlockTypes().forEach(block -> quoted.add("\"" + block.key(RegistryTypes.BLOCK_TYPE).formatted() + "\""));
            sql.append(" AND target IN (").append(String.join(", ", quoted)).append(")");
        }

        try (final Connection conn = getConn()) {
            final PreparedStatement ps = conn.prepareStatement(sql.toString());
            ps.setString(1, query.getWorldKey().formatted());

            // Ranged lookup

            // Rearrange from smallest to biggest for things to actually get picked up
            int[] x = { query.getMinimumPosition().x(), query.getMaximumPosition().x() };
            int[] y = { query.getMinimumPosition().y(), query.getMaximumPosition().y() };
            int[] z = { query.getMinimumPosition().z(), query.getMaximumPosition().z() };

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

            List<EspialRecord> records = new LinkedList<>();
            final ResultSet rs = ps.executeQuery();

            while (rs.next()) {
//                System.out.println("------------------------------");
//                final ResultSetMetaData rsmd = rs.getMetaData();
//                final int columnsNumber = rsmd.getColumnCount();
//                for (int i = 1; i <= columnsNumber; i++) {
//                    final String columnValue = rs.getString(i);
//                    System.out.println(rsmd.getColumnName(i) + " : " + columnValue);
//                }

                records.add(RecordFactoryProvider.create(rs));
            }
            // TODO: filter events in SQL
            if (!query.getEvents().isEmpty()) {
                records = records.stream().filter(record -> query.getEvents().contains(record.getEvent())).toList();
            }
            return records;
        }
    }

    public void batchSetRolledBack(@NotNull final List<EspialRecord> records, final boolean rolledBack) throws SQLException {
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
        }
    }

    public void setRolledBack(@NotNull final EspialRecord record, final boolean rolledBack) throws SQLException {
        try (final Connection conn = getConn()) {
            final PreparedStatement ps = conn.prepareStatement("UPDATE records SET rolled_back = ? WHERE id = ?");

            if (rolledBack) {
                ps.setDate(1, new java.sql.Date(System.currentTimeMillis()));
            } else {
                ps.setNull(1, Types.DATE);
            }

            ps.setInt(2, record.getId());
            ps.executeUpdate();
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

    private static String componentToString(final Component component) {
        return GsonComponentSerializer.gson().serialize(component);
    }

    public void delete(final int id) throws SQLException {
        try (final Connection conn = getConn()) {
            final PreparedStatement ps = conn.prepareStatement("DELETE FROM records WHERE id = ?");
            ps.setInt(1, id);
            ps.execute();
        }
    }

    public void batchDelete(final List<Integer> ids) throws SQLException {
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
        }
    }

}
