package net.slimediamond.espial.sponge.storage.sql;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.api.SignText;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.SignModifyRecord;
import net.slimediamond.espial.api.storage.EspialStorageException;
import net.slimediamond.espial.sponge.record.SpongeSignModifyRecord;
import net.slimediamond.espial.sponge.storage.Result;
import net.slimediamond.espial.sponge.storage.SpongeRecordStorage;
import org.spongepowered.api.block.BlockState;

import java.sql.*;
import java.util.*;

public class SQLSignModifyStorage extends SpongeRecordStorage<SignModifyRecord> {

    final SQLStorage sqlStorage;

    public SQLSignModifyStorage(final SQLStorage sqlStorage) {
        super(SignModifyRecord.class);
        this.sqlStorage = sqlStorage;

        this.supports(EspialEvents.SIGN_MODIFY);
    }

    @Override
    public List<SignModifyRecord> query(final EspialQuery query) throws EspialStorageException {
        final List<SignModifyRecord> results = new ArrayList<>();
        try (final Connection conn = this.sqlStorage.getConn()) {
            final String sql = """
                        SELECT records_view.id AS id, time, rolled_back, player_uuid, x, y, z, type, world_key, entity_type_key,
                        original.state AS original_state, replacement.state AS replacement_state,
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
                        signs_replacement.back_4  AS replacement_back_4
                        FROM records_view
                        LEFT JOIN sign_change ON records_view.id = sign_change.record_id
                        LEFT JOIN sign_text AS signs_original ON sign_change.original_text = signs_original.id
                        LEFT JOIN sign_text AS signs_replacement ON sign_change.replacement_text = signs_replacement.id
                        LEFT JOIN block_change AS bs ON records_view.id = bs.record_id
                        LEFT JOIN block_states AS original ON bs.original_block = original.id
                        LEFT JOIN block_states AS replacement ON bs.replacement_block = replacement.id
                    """;

            final ResultSet rs = this.sqlStorage.query(conn, sql, query, this);
            while (rs.next()) {

                final Result result = SQLStorage.buildRecord(rs);

                // Fuck

                final Component originalFront1 = getComponent(rs.getString("original_front_1"));
                final Component originalFront2 = getComponent(rs.getString("original_front_2"));
                final Component originalFront3 = getComponent(rs.getString("original_front_3"));
                final Component originalFront4 = getComponent(rs.getString("original_front_4"));

                final Component originalBack1 = getComponent(rs.getString("original_back_1"));
                final Component originalBack2 = getComponent(rs.getString("original_back_2"));
                final Component originalBack3 = getComponent(rs.getString("original_back_3"));
                final Component originalBack4 = getComponent(rs.getString("original_back_4"));

                final Component replacementFront1 = getComponent(rs.getString("replacement_front_1"));
                final Component replacementFront2 = getComponent(rs.getString("replacement_front_2"));
                final Component replacementFront3 = getComponent(rs.getString("replacement_front_3"));
                final Component replacementFront4 = getComponent(rs.getString("replacement_front_4"));

                final Component replacementBack1 = getComponent(rs.getString("replacement_back_1"));
                final Component replacementBack2 = getComponent(rs.getString("replacement_back_2"));
                final Component replacementBack3 = getComponent(rs.getString("replacement_back_3"));
                final Component replacementBack4 = getComponent(rs.getString("replacement_back_4"));

                final SignText originalText = SignText.from(originalFront1, originalFront2, originalFront3, originalFront4,
                        originalBack1, originalBack2, originalBack3, originalBack4);

                final SignText replacementText = SignText.from(replacementFront1, replacementFront2, replacementFront3, replacementFront4,
                        replacementBack1, replacementBack2, replacementBack3, replacementBack4);

                final BlockState blockState = BlockState.fromString(rs.getString("original_state"));

                results.add(new SpongeSignModifyRecord(
                        result.id(),
                        result.date(),
                        result.user(),
                        result.entityType(),
                        result.location(),
                        result.event(),
                        result.rolledBack(),
                        originalText,
                        replacementText,
                        true, // FIXME: This is always true which doesn't seem right?
                        blockState
                ));
            }

        } catch (final SQLException e) {
            throw new EspialStorageException("Unable to query records", e, Collections.emptyList());
        }
        return results;
    }

    private static Component getComponent(final String string) {
        if (string == null) {
            return Component.empty();
        }
        return GsonComponentSerializer.gson().deserialize(string);
    }

    @Override
    public int submit(final SignModifyRecord record) throws EspialStorageException {
        try (final Connection conn = this.sqlStorage.getConn()) {
            final int id = this.sqlStorage.insert(conn, record);

            // TODO: ideally each side is stored individually (as opposed to both sides)
            // or heck even each line, but that's way too much effort :)
            final SignText original = record.getOriginalContents();
            final SignText replacement = record.getReplacementContents();

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

            final int originalId = this.sqlStorage.getOrCreateId(conn, "sign_text", originalSign, Types.VARCHAR);
            final int replacementId = this.sqlStorage.getOrCreateId(conn, "sign_text", replacementSign, Types.VARCHAR);

            final PreparedStatement insertSign = conn.prepareStatement("INSERT INTO sign_change (record_id, original_text, replacement_text) VALUES (?, ?, ?)");
            insertSign.setInt(1, id);
            insertSign.setInt(2, originalId);
            insertSign.setInt(3, replacementId);
            insertSign.execute();

            final int state = this.sqlStorage.getOrCreateId(conn, "block_states", "state",
                    record.getBlockState().asString());

            final PreparedStatement insertState = conn.prepareStatement("INSERT INTO block_change (record_id, original_block, replacement_block) " +
                    "VALUES (?, ?, ?)");

            insertState.setInt(1, id);
            insertState.setInt(2, state);
            insertState.setInt(3, state);
            insertState.execute();

            return id;
        } catch (final SQLException e) {
            throw new EspialStorageException(e, record);
        }
    }

    private static String componentToString(final Component component) {
        return GsonComponentSerializer.gson().serialize(component);
    }

}
