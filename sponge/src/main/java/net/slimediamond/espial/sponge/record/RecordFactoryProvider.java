package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.record.EspialRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class RecordFactoryProvider {

    private static final Map<EspialEvent, RecordFactory<? extends EspialRecord>> RECORD_TYPES = Map.of(
            EspialEvents.BREAK.get(),         new SpongeBlockRecordFactory(),
            EspialEvents.PLACE.get(),         new SpongeBlockRecordFactory(),
            EspialEvents.DECAY.get(),         new SpongeBlockRecordFactory(),
            EspialEvents.GROWTH.get(),        new SpongeBlockRecordFactory(),
            EspialEvents.LIQUID_DECAY.get(),  new SpongeBlockRecordFactory(),
            EspialEvents.MODIFY.get(),        new SpongeBlockRecordFactory(),
            EspialEvents.INTERACT.get(),      new SpongeBlockRecordFactory(),
            EspialEvents.HANGING_DEATH.get(), new SpongeHangingDeathRecordFactory(),
            EspialEvents.SIGN_MODIFY.get(),   new SpongeSignModifyRecordFactory()
    );

    @SuppressWarnings("unchecked")
    public static <T extends EspialRecord> T create(@NotNull final ResultSet rs) throws SQLException {
        final int type = rs.getInt("type");
        final EspialEvent event = EspialEvents.registry().streamEntries()
                .map(RegistryEntry::value)
                .filter(e -> e.getId() == type)
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
        final boolean rolledBack = rs.getBoolean("rolled_back");

        return (T) RECORD_TYPES.get(event).create(event, rs, id, date, user, entityType, location, rolledBack);
    }

}
