package net.slimediamond.espial.sponge.storage.sql;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.ContainerChangeRecord;
import net.slimediamond.espial.api.record.HangingDeathRecord;
import net.slimediamond.espial.api.storage.EspialStorageException;
import net.slimediamond.espial.sponge.record.SpongeContainerChangeRecord;
import net.slimediamond.espial.sponge.record.SpongeHangingDeathRecord;
import net.slimediamond.espial.sponge.storage.RecordStorage;
import net.slimediamond.espial.sponge.storage.Result;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryTypes;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SQLHangingDeathStorage implements RecordStorage<HangingDeathRecord> {

    final SQLStorage sqlStorage;

    public SQLHangingDeathStorage(final SQLStorage sqlStorage) {
        this.sqlStorage = sqlStorage;
    }

    @Override
    public List<HangingDeathRecord> query(final EspialQuery query) throws EspialStorageException {
        final List<HangingDeathRecord> results = new ArrayList<>();
        try (final Connection conn = this.sqlStorage.getConn()) {
            final String sql = """
                SELECT records_view.id AS id, time, rolled_back, player_uuid, x, y, z, type, world_key, entity_type_key,
                extra.original_data as original_data, entity_types.resource_key AS target
                FROM records_view
                LEFT JOIN extra ON extra.record_id = records_view.id
                LEFT JOIN hanging_deaths ON hanging_deaths.record_id = records_view.id
                LEFT JOIN entity_types ON entity_types.id = hanging_deaths.entity_type
            """;

            final ResultSet rs = this.sqlStorage.query(conn, sql, query, this);
            while (rs.next()) {

                final Result result = SQLStorage.buildRecord(rs);
                final String target = rs.getString("target");
                final EntityType<?> targetEntityType = EntityTypes.registry().value(ResourceKey.resolve(target));
                final String extra = rs.getString("original_data");

                final DataContainer originalData;
                if (extra == null || extra.isEmpty()) {
                    originalData = DataContainer.createNew(); // empty container
                } else {
                    originalData = DataFormats.JSON.get().read(extra);
                }

                results.add(new SpongeHangingDeathRecord(
                        result.id(),
                        result.date(),
                        result.user(),
                        result.entityType(),
                        result.location(),
                        result.event(),
                        result.rolledBack(),
                        targetEntityType,
                        originalData
                ));
            }

        } catch (final SQLException | IOException e) {
            throw new EspialStorageException("Could not query records", e, Collections.emptyList());
        }
        return results;
    }

    @Override
    public int submit(final HangingDeathRecord record) throws EspialStorageException {
        try (final Connection conn = this.sqlStorage.getConn()) {
            final int id = this.sqlStorage.insert(conn, record);

            final PreparedStatement insertEntity = conn.prepareStatement("INSERT INTO hanging_deaths (record_id, entity_type) VALUES (?, ?)");
            final int entityId = this.sqlStorage.getOrCreateId(
                    conn,
                    "entity_types",
                    "resource_Key",
                    record.getTargetEntityType().key(RegistryTypes.ENTITY_TYPE).formatted()
            );
            insertEntity.setInt(1, id);
            insertEntity.setInt(2, entityId);
            insertEntity.execute();
            return id;
        } catch (final SQLException e) {
            throw new EspialStorageException("Unable to insert record", e, record);
        }
    }

    @Override
    public Class<HangingDeathRecord> getType() {
        return HangingDeathRecord.class;
    }

    @Override
    public Set<EspialEvent> getHandledEvents() {
        return Set.of(EspialEvents.HANGING_DEATH.get());
    }

}
