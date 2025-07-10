package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.sponge.Espial;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public final class SpongeBlockRecordFactory implements RecordFactory<EspialBlockRecord> {

    @Override
    public EspialBlockRecord create(@NotNull final EspialEvent event, @NotNull final ResultSet rs) throws SQLException {
        final int id = rs.getInt("id");
        final Date date = Date.from(rs.getTimestamp("time").toInstant());

        UUID user = null;
        String playerUuid = rs.getString("player_uuid");
        if (playerUuid != null) {
            user = UUID.fromString(playerUuid);
        }

        final EntityType<?> entityType = EntityTypes.registry()
                .value(ResourceKey.resolve(rs.getString("entity_type")));
        final BlockState blockState = BlockState.fromString(rs.getString("target"));
        final ResourceKey worldKey = ResourceKey.resolve(rs.getString("world"));
        final int x = rs.getInt("x");
        final int y = rs.getInt("y");
        final int z = rs.getInt("z");
        final boolean rolledBack = rs.getBoolean("rolled_back");

        DataContainer extraData = null;
        final String extraRaw = rs.getString("data");
        if (extraRaw != null) {
            try {
                extraData = DataFormats.JSON.get().read(extraRaw);
            } catch (IOException e) {
                // construct the record with no extra data anyway
                Espial.getInstance().getLogger().error("Unable to read extra data from EspialRecord. " +
                        "It will be constructed without it", e);
            }
        }

        final ServerLocation serverLocation = ServerLocation.of(worldKey, Vector3i.from(x, y, z));

        return new SpongeBlockRecord(id, date, user, entityType, serverLocation, event, blockState, rolledBack, extraData);
    }

}
