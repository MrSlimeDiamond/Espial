package net.slimediamond.espial.sponge.storage;

import com.google.gson.JsonArray;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.api.event.InsertRecordEvent;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.HangingDeathRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.record.SignModifyRecord;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class EspialDatabase {

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
        config.setMaximumPoolSize(2); // TODO config value?
        config.setMaximumPoolSize(4); // TODO config value?
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setValidationTimeout(5000); // 5 seconds
        config.setKeepaliveTime(300000); // 5 mins
        config.setConnectionTestQuery("SELECT 1");
        dataSource = new HikariDataSource(config);

        try (final Connection conn = getConn()) {
            final String recordsCreation;
            final String blockStatesCreation;
            final String entityTypesCreation;
            final String worldsCreation;
            final String extraCreation = "CREATE TABLE IF NOT EXISTS extra (" +
                    "record_id INT NOT NULL, " +
                    "original TEXT, " +
                    "replacement TEXT, " +
                    "FOREIGN KEY (record_id) REFERENCES records(id) ON DELETE CASCADE" +
                    ")";

            final String blockStateCreation = "CREATE TABLE IF NOT EXISTS block_state (" +
                    "record_id INT NOT NULL, " +
                    "original INT NOT NULL, " +
                    "replacement INT NOT NULL, " +
                    "FOREIGN KEY (record_id) REFERENCES records(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (original) REFERENCES block_states(id), " +
                    "FOREIGN KEY (replacement) REFERENCES block_states(id)" +
                    ")";

            // Databases need to be made differently on different databases.
            this.sqlite = connectionString.contains("sqlite");
            if (sqlite) {
                Espial.getInstance().getLogger().info("Detected database type: sqlite");

                recordsCreation = "CREATE TABLE IF NOT EXISTS records (id INTEGER PRIMARY KEY AUTOINCREMENT, ";
                blockStatesCreation = "CREATE TABLE IF NOT EXISTS block_states (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, ";
                entityTypesCreation = "CREATE TABLE IF NOT EXISTS entity_types (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, ";
                worldsCreation = "CREATE TABLE IF NOT EXISTS worlds (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, ";
            } else {
                // Probably MySQL/MariaDB or whatever. use a different statement

                Espial.getInstance().getLogger().info("Detected database type: MySQL/MariaDB");

                recordsCreation = "CREATE TABLE IF NOT EXISTS records (id INT AUTO_INCREMENT PRIMARY KEY, ";
                blockStatesCreation = "CREATE TABLE IF NOT EXISTS block_states (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, ";
                entityTypesCreation = "CREATE TABLE IF NOT EXISTS entity_types (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, ";
                worldsCreation = "CREATE TABLE IF NOT EXISTS worlds (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, ";
            }

            conn.prepareStatement(recordsCreation +
                    "type TINYINT NOT NULL, " +
                    "time TIMESTAMP NOT NULL, " +
                    "player_uuid CHAR(36), " +
                    "entity_type INT NOT NULL, " +
                    "target TINYTEXT NOT NULL, " +
                    "world INT NOT NULL, " +
                    "x INT NOT NULL, " +
                    "y INT NOT NULL, " +
                    "z INT NOT NULL, " +
                    "rolled_back BOOLEAN NOT NULL DEFAULT FALSE, " +
                    "FOREIGN KEY (entity_type) REFERENCES entity_types(id), " +
                    "FOREIGN KEY (world) REFERENCES worlds(id)" +
                    ")").execute();

            conn.prepareStatement(blockStatesCreation + "state TINYTEXT NOT NULL)").execute();
            conn.prepareStatement(blockStateCreation).execute();
            conn.prepareStatement(entityTypesCreation + "resource_key TEXT NOT NULL)").execute();
            conn.prepareStatement(worldsCreation + "resource_key TEXT NOT NULL)").execute();
            conn.prepareStatement(extraCreation).execute();

            if (sqlite) {
                conn.prepareStatement("PRAGMA foreign_keys = ON").execute();
                // prevent database file locking on sqlite
                //conn.prepareStatement("PRAGMA journal_mode = WAL").execute();
            }
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
            final PreparedStatement ps = conn.prepareStatement("INSERT INTO records "
                    + " (type," +
                    "time," +
                    "player_uuid," +
                    "entity_type," +
                    "target," +
                    "world," +
                    "x," +
                    "y," +
                    "z," +
                    "rolled_back)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE)",
                    Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, record.getEvent().getId());
            final Timestamp timestamp = new Timestamp(record.getDate().getTime());
            ps.setTimestamp(2, timestamp);
            if (record.getUser().isPresent()) {
                ps.setString(3, record.getUser().get().toString());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }

            // this makes stuff slower, but it's much more efficient for storage
            final int entityTypeId = getOrCreateId(conn, "entity_types", "resource_key",
                    record.getEntityType().key(RegistryTypes.ENTITY_TYPE).formatted());
            final int worldId = getOrCreateId(conn, "worlds", "resource_key",
                    record.getLocation().worldKey().formatted());

            ps.setInt(4, entityTypeId);
            ps.setString(5, record.getTarget());
            ps.setInt(6, worldId);
            ps.setInt(7, record.getLocation().blockPosition().x());
            ps.setInt(8, record.getLocation().blockPosition().y());
            ps.setInt(9, record.getLocation().blockPosition().z());

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
                final PreparedStatement insertExtra = conn.prepareStatement("INSERT INTO extra (record_id, original, replacement) " +
                        "VALUES (?, ?, ?)");

                if (record instanceof final BlockRecord blockRecord) {
                    // insert into block_state
                    final int originalState = getOrCreateId(conn, "block_states", "state",
                            blockRecord.getOriginalBlock().state().asString());
                    final int replacementState = getOrCreateId(conn, "block_states", "state",
                            blockRecord.getReplacementBlock().state().asString());

                    final PreparedStatement insertState = conn.prepareStatement("INSERT INTO block_state (record_id, original, replacement) " +
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
                        }
                        if (blockRecord.getReplacementBlock().toContainer().contains(unsafeData)) {
                            insertExtra.setString(3, DataFormats.JSON.get()
                                    .write(blockRecord.getReplacementBlock().toContainer()));
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
                    // turn sign stuff into json stuff

                    final JsonArray original = new JsonArray();
                    signModifyRecord.getOriginalContents().stream()
                            .map(component -> GsonComponentSerializer.gson().serializeToTree(component))
                            .forEach(original::add);
                    final JsonArray replacement = new JsonArray();
                    signModifyRecord.getReplacementContents().stream()
                            .map(component -> GsonComponentSerializer.gson().serializeToTree(component))
                            .forEach(replacement::add);

                    insertExtra.setInt(1, id);
                    insertExtra.setString(2, original.toString());
                    insertExtra.setString(3, replacement.toString());
                    insertExtra.execute();

                    final int state = getOrCreateId(conn, "block_states", "state",
                            signModifyRecord.getBlockState().asString());

                    final PreparedStatement insertState = conn.prepareStatement("INSERT INTO block_state (record_id, original, replacement) " +
                            "VALUES (?, ?, ?)");

                    insertState.setInt(1, id);
                    insertState.setInt(2, state);
                    insertState.setInt(3, state);
                    insertState.execute();
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
                "SELECT " +
                        "records.*, " +
                        "extra.original AS extra_original, " +
                        "extra.replacement AS extra_replacement, " +
                        "original.state AS state_original, " +
                        "replacement.state AS state_replacement, " +
                        "worlds.resource_key AS world_key, " +
                        "entity_types.resource_key AS entity_type_key " +
                        "FROM records " +
                        "LEFT JOIN extra ON records.id = extra.record_id " +
                        "LEFT JOIN block_state ON records.id = block_state.record_id " +
                        "LEFT JOIN block_state AS bs ON records.id = bs.record_id " +
                        "LEFT JOIN block_states AS original ON bs.original = original.id " +
                        "LEFT JOIN block_states AS replacement ON bs.replacement = replacement.id " +
                        "JOIN entity_types ON records.entity_type = entity_types.id " +
                        "JOIN worlds ON records.world = worlds.id " +
                        "WHERE worlds.resource_key = ? " +
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
        try (final Connection conn = getConn()) {
            final String placeholders = String.join(", ", Collections.nCopies(records.size(), "?"));
            final PreparedStatement ps = conn.prepareStatement(
                    "UPDATE records SET rolled_back = ? WHERE id IN (" + placeholders + ")"
            );
            ps.setBoolean(1, rolledBack);
            for (int i = 0; i < records.size(); i++) {
                ps.setInt(i + 2, records.get(i).getId());
            }
            ps.executeUpdate();
        }
    }

    public void setRolledBack(@NotNull final EspialRecord record, final boolean rolledBack) throws SQLException {
        try (final Connection conn = getConn()) {
            final PreparedStatement ps = conn.prepareStatement("UPDATE records SET rolled_back = ? WHERE id = ?");

            ps.setBoolean(1, rolledBack);
            ps.setInt(2, record.getId());
            ps.executeUpdate();
        }
    }

    private int getOrCreateId(final Connection conn, final String table, final String column, final String value) throws SQLException {
        // Try to find existing
        final PreparedStatement select = conn.prepareStatement("SELECT id FROM " + table + " WHERE " + column + " = ?");
        select.setString(1, value);
        final ResultSet rs = select.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }

        // Insert if not exists
        final PreparedStatement insert = conn.prepareStatement("INSERT INTO " + table + " (" + column + ") VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        insert.setString(1, value);
        insert.executeUpdate();
        final ResultSet keys;
        if (sqlite) {
            keys = conn.prepareStatement("SELECT last_insert_rowid()").executeQuery();
        } else {
            keys = insert.getGeneratedKeys();
        }
        keys.next();
        return keys.getInt(1);
    }

}
