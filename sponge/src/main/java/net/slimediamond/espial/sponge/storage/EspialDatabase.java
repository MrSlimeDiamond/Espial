package net.slimediamond.espial.sponge.storage;

import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.record.RecordFactoryProvider;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.registry.RegistryTypes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class EspialDatabase {

    private final String connectionString;
    private final String recordsTableName;

    public EspialDatabase(@NotNull final String connectionString,
                          @NotNull final String recordsTableName) {
        this.connectionString = connectionString;
        this.recordsTableName = recordsTableName;
    }

    private Connection getConn() throws SQLException {
        return DriverManager.getConnection(connectionString);
    }

    /**
     * Create database tables
     *
     * @throws SQLException
     */
    public void createTables() throws SQLException {
        try (final Connection conn = getConn()) {
            final String creation;
            final String legacyCheck;
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
                    "type TINYINT NOT NULL, " +
                    "time TIMESTAMP NOT NULL, " +
                    "player_uuid MEDIUMTEXT, " +
                    "target TINYTEXT NOT NULL, " +
                    "world TINYTEXT NOT NULL, " +
                    "x INT NOT NULL, " +
                    "y INT NOT NULL, " +
                    "z INT NOT NULL, " +
                    "rolled_back BOOLEAN NOT NULL DEFAULT FALSE" +
                    ")").execute();

            final boolean hasLegacyTable = conn.prepareStatement(legacyCheck).executeQuery().next();
            if (hasLegacyTable) {
                try {
                    conn.prepareStatement(
                            "INSERT INTO records (type, time, player_uuid, block_id, world, x, y, z, rolled_back) " +
                                    "SELECT type, time, player_uuid, target, world, x, y, z, rolled_back FROM blocklog").execute();
                    conn.prepareStatement("DROP TABLE IF EXISTS blocklog").execute(); // remove old table
                    Espial.getInstance().getLogger().info("Database migrated");
                } catch (SQLException ignored) {
                    // Does not need to be migrated
                }
            }
        }
    }

    /**
     * Submit a {@link EspialBlockRecord} to the database.
     *
     * @param record The record to insert
     * @return The ID primary key of the inserted record
     * @throws SQLException
     */
    public int submit(@NotNull final EspialBlockRecord record) throws SQLException {
        try (final Connection conn = getConn()) {
            final PreparedStatement ps = conn.prepareStatement("INSERT INTO " + this.recordsTableName
                    + " (type," +
                    "time," +
                    "player_uuid," +
                    "target," +
                    "world," +
                    "x," +
                    "y," +
                    "z," +
                    "rolled_back)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE)",
                    Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, record.getEvent().getId());
            final Timestamp timestamp = new Timestamp(record.getDate().getTime());
            ps.setTimestamp(2, timestamp);
            if (record.getUser().isPresent()) {
                ps.setString(3, record.getUser().get().toString());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setString(4, record.getBlockState().asString());
            ps.setString(5, record.getLocation().worldKey().formatted());
            ps.setInt(6, record.getLocation().blockPosition().x());
            ps.setInt(7, record.getLocation().blockPosition().y());
            ps.setInt(8, record.getLocation().blockPosition().z());

            ps.executeUpdate();

            ResultSet rs;
            try {
                rs = ps.getGeneratedKeys();
            } catch (SQLFeatureNotSupportedException e) {
                // Try to use another approach (this should work for sqlite)
                rs = conn.prepareStatement("SELECT last_insert_rowid()").executeQuery();
            }
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }

    public List<EspialRecord> query(@NotNull final EspialQuery query) throws SQLException {
        final StringBuilder sql = new StringBuilder("SELECT * FROM " + recordsTableName + " WHERE world" +
                " = ?");

        sql.append(" AND x BETWEEN ? and ? AND y BETWEEN ? and ? AND z BETWEEN ? and ? ");

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

    public void setRolledBack(@NotNull final EspialRecord record, final boolean rolledBack) throws SQLException {
        try (final Connection conn = getConn()) {
            final PreparedStatement ps = conn.prepareStatement("UPDATE " + recordsTableName + " SET rolled_back = ? WHERE id = ?");

            ps.setBoolean(1, rolledBack);
            ps.setInt(2, record.getId());
            ps.executeUpdate();
        }
    }

}
