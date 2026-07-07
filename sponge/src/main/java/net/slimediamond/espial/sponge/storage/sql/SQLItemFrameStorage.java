package net.slimediamond.espial.sponge.storage.sql;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.ContainerChangeRecord;
import net.slimediamond.espial.api.record.ItemFrameChangeRecord;
import net.slimediamond.espial.api.storage.EspialStorageException;
import net.slimediamond.espial.sponge.record.SpongeContainerChangeRecord;
import net.slimediamond.espial.sponge.record.SpongeItemFrameChangeRecord;
import net.slimediamond.espial.sponge.storage.RecordStorage;
import net.slimediamond.espial.sponge.storage.Result;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SQLItemFrameStorage implements RecordStorage<ItemFrameChangeRecord> {

    final SQLStorage sqlStorage;

    public SQLItemFrameStorage(final SQLStorage sqlStorage) {
        this.sqlStorage = sqlStorage;
    }

    @Override
    public List<ItemFrameChangeRecord> query(final EspialQuery query) throws EspialStorageException {
        final List<ItemFrameChangeRecord> results = new ArrayList<>();
        try (final Connection conn = this.sqlStorage.getConn()) {
            final String sql = """
                SELECT records_view.id AS id, time, rolled_back, player_uuid, x, y, z, type, world_key, entity_type_key,
                item_original.data AS item_original, item_replacement.data AS item_replacement
                FROM records_view
                LEFT JOIN item_container ON item_container.record_id = records_view.id
                LEFT JOIN items AS item_original ON item_original.id = item_container.original_item
                LEFT JOIN items AS item_replacement ON item_replacement.id = item_container.replacement_item
            """;

            final ResultSet rs = this.sqlStorage.query(conn, sql, query, this);
            while (rs.next()) {

                final Result result = SQLStorage.buildRecord(rs);
                final ItemStackSnapshot original = ItemStack.builder()
                        .fromContainer(DataFormats.JSON.get().read(rs.getString("item_original")))
                        .build()
                        .asImmutable();
                final ItemStackSnapshot replacement = ItemStack.builder()
                        .fromContainer(DataFormats.JSON.get().read(rs.getString("item_replacement")))
                        .build()
                        .asImmutable();

                results.add(new SpongeItemFrameChangeRecord(
                        result.id(),
                        result.date(),
                        result.user(),
                        result.entityType(),
                        result.location(),
                        result.event(),
                        result.rolledBack(),
                        original,
                        replacement
                ));
            }

        } catch (final SQLException | IOException e) {
            throw new EspialStorageException(e);
        }
        return results;
    }

    @Override
    public int submit(final ItemFrameChangeRecord record) throws EspialStorageException {
        // TODO remove duplicate code?
        try (final Connection conn = this.sqlStorage.getConn()) {
            final int id = this.sqlStorage.insert(conn, record);

            final int original = this.sqlStorage.getOrCreateId(
                    conn,
                    "items",
                    "data",
                    DataFormats.JSON.get().write(record.getOriginal().toContainer())
            );
            final int replacement = this.sqlStorage.getOrCreateId(
                    conn,
                    "items",
                    "data",
                    DataFormats.JSON.get().write(record.getReplacement().toContainer())
            );
            final PreparedStatement insertItem = conn.prepareStatement("INSERT INTO item_container (record_id, original_item, replacement_item) VALUES (?, ?, ?)");
            insertItem.setInt(1, id);
            insertItem.setInt(2, original);
            insertItem.setInt(3, replacement);
            insertItem.execute();
            return id;
        } catch (final SQLException | IOException e) {
            throw new EspialStorageException(e, record);
        }
    }

    @Override
    public Class<ItemFrameChangeRecord> getType() {
        return ItemFrameChangeRecord.class;
    }

    @Override
    public Set<EspialEvent> getHandledEvents() {
        return Set.of(EspialEvents.ITEM_FRAME_INSERT.get(), EspialEvents.ITEM_FRAME_REMOVE.get());
    }

}
