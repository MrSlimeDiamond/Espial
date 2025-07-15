package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialHangingDeathRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class SpongeHangingDeathRecordFactory implements RecordFactory<EspialHangingDeathRecord> {

    @Override
    public EspialHangingDeathRecord create(@NotNull final EspialEvent event,
                                           @NotNull final ResultSet rs,
                                           final int id,
                                           @NotNull final Date date,
                                           @Nullable final UUID user,
                                           @NotNull final EntityType<?> entityType,
                                           @NotNull final ServerLocation location,
                                           final boolean rolledBack)
            throws SQLException {
        final String target = rs.getString("target");
        final EntityType<?> targetEntityType = EntityTypes.registry().value(ResourceKey.resolve(target));
        return new SpongeHangingDeathRecord(date, user, entityType, location, event, rolledBack, targetEntityType, null);
    }

}
