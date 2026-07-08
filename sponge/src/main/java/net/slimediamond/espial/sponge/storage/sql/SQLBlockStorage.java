package net.slimediamond.espial.sponge.storage.sql;

import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.storage.EspialStorageException;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.record.SpongeBlockRecord;
import net.slimediamond.espial.sponge.storage.SpongeRecordStorage;
import net.slimediamond.espial.sponge.storage.Result;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLBlockStorage extends SpongeRecordStorage<BlockRecord> {

    private final SQLStorage sqlStorage;

    public SQLBlockStorage(final SQLStorage sqlStorage) {
        super(BlockRecord.class);
        this.sqlStorage = sqlStorage;

        this.supports(EspialEvents.PLACE, EspialEvents.BREAK, EspialEvents.GROWTH, EspialEvents.MODIFY);
    }

    @Override
    public List<BlockRecord> query(final EspialQuery query) throws EspialStorageException {
        final List<BlockRecord> results = new ArrayList<>();
        try (final Connection conn = this.sqlStorage.getConn()) {
            final String sql = """
                SELECT records_view.id AS id, time, rolled_back, player_uuid, x, y, z, type, world_key, entity_type_key,
                extra.original_data AS original_data, extra.replacement_data as replacement_data,
                original.state AS original_state, replacement.state AS replacement_state
                FROM records_view
                LEFT JOIN block_change AS bs ON records_view.id = bs.record_id
                LEFT JOIN block_states AS original ON bs.original_block = original.id
                LEFT JOIN block_states AS replacement ON bs.replacement_block = replacement.id
                LEFT JOIN extra ON extra.record_id = records_view.id
            """;

            final ResultSet rs = this.sqlStorage.query(conn, sql, query, this);
            while (rs.next()) {
                final Result result = SQLStorage.buildRecord(rs);
                final ResourceKey worldKey = result.location().worldKey();
                final BlockState originalState = BlockState.fromString(rs.getString("original_state"));
                final BlockState replacementState = BlockState.fromString(rs.getString("replacement_state"));

                BlockSnapshot original = BlockSnapshot.builder()
                        .blockState(originalState)
                        .world(Sponge.server().worldManager().world(worldKey).orElseThrow().properties())
                        .position(result.location().blockPosition())
                        .build();

                BlockSnapshot replacement = BlockSnapshot.builder()
                        .blockState(replacementState)
                        .world(Sponge.server().worldManager().world(worldKey).orElseThrow().properties())
                        .position(result.location().blockPosition())
                        .build();

                // see if we have original_data or replacement_data, then apply its data.
                // It's not always present for storage space purposes.
                try {
                    final String extraOriginal = rs.getString("original_data");
                    if (extraOriginal != null) {
                        original = original.withRawData(DataFormats.JSON.get().read(extraOriginal));
                    }
                    final String extraReplacement = rs.getString("replacement_data");
                    if (extraReplacement != null) {
                        replacement = replacement.withRawData(DataFormats.JSON.get().read(extraReplacement));
                    }
                } catch (Exception e) {
                    Espial.getInstance().getLogger().error("Unable to get raw data for EspialBlockRecord", e);
                }

                results.add(new SpongeBlockRecord(
                        result.id(),
                        result.date(),
                        result.user(),
                        result.entityType(),
                        result.location(),
                        result.event(),
                        original,
                        replacement,
                        result.rolledBack())
                );
            }

        } catch (final SQLException e) {
            throw new EspialStorageException(e);
        }
        return results;
    }

    @Override
    public int submit(final BlockRecord record) throws EspialStorageException {
        try (final Connection conn = this.sqlStorage.getConn()) {
            final int id = this.sqlStorage.insert(conn, record);

            final DataQuery unsafeData = DataQuery.of("UnsafeData");
            final PreparedStatement insertExtra = this.sqlStorage.prepareExtraDataStatement(conn);

            // insert into block_state
            final int originalState = this.sqlStorage.getOrCreateId(conn, "block_states", "state",
                    record.getOriginalBlock().state().asString());
            final int replacementState = this.sqlStorage.getOrCreateId(conn, "block_states", "state",
                    record.getReplacementBlock().state().asString());

            final PreparedStatement insertState = conn.prepareStatement("INSERT INTO block_change (record_id, original_block, replacement_block) " +
                    "VALUES (?, ?, ?)");

            insertState.setInt(1, id);
            insertState.setInt(2, originalState);
            insertState.setInt(3, replacementState);
            insertState.execute();

            if (record.getOriginalBlock().toContainer().contains(unsafeData)
                    || record.getReplacementBlock().toContainer().contains(unsafeData)) {
                insertExtra.setInt(1, id);
                if (record.getOriginalBlock().toContainer().contains(unsafeData)) {
                    insertExtra.setString(2, DataFormats.JSON.get()
                            .write(record.getOriginalBlock().toContainer()));
                } else {
                    insertExtra.setNull(2, Types.CHAR);
                }
                if (record.getReplacementBlock().toContainer().contains(unsafeData)) {
                    insertExtra.setString(3, DataFormats.JSON.get()
                            .write(record.getReplacementBlock().toContainer()));
                } else {
                    insertExtra.setNull(3, Types.CHAR);
                }
                insertExtra.execute();
            }

            return id;
        } catch (final SQLException | IOException e) {
            throw new EspialStorageException(e, record);
        }
    }

}
