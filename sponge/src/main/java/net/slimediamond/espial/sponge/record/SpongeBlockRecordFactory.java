package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.sponge.Espial;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SpongeBlockRecordFactory implements RecordFactory<EspialBlockRecord> {

    @Override
    public CompletableFuture<EspialBlockRecord> create(@NotNull final EspialEvent event, @NotNull final ResultSet rs) throws SQLException {
        final CompletableFuture<EspialBlockRecord> future = new CompletableFuture<>();
        final int id = rs.getInt("id");
        final Date date = Date.from(rs.getTimestamp("time").toInstant());

        final UUID user;
        String playerUuid = rs.getString("player_uuid");
        if (playerUuid != null) {
            user = UUID.fromString(playerUuid);
        } else {
            user = null;
        }

        final EntityType<?> entityType = EntityTypes.registry()
                .value(ResourceKey.resolve(rs.getString("entity_type")));
        final ResourceKey worldKey = ResourceKey.resolve(rs.getString("world_key"));
        final int x = rs.getInt("x");
        final int y = rs.getInt("y");
        final int z = rs.getInt("z");
        final boolean rolledBack = rs.getBoolean("rolled_back");
        final ServerLocation location = ServerLocation.of(worldKey, Vector3i.from(x, y, z));
        final String originalStateString = rs.getString("state_original");
        final String replacementStateString = rs.getString("state_replacement");

        Sponge.server().scheduler().submit(Task.builder()
                .execute(() -> {
                    // this needs to be on the server thread for some reason
                    // FIXME: This is silly
                    // this seems to be really slow with tile entities
                    BlockSnapshot original = BlockState.fromString(originalStateString).snapshotFor(location);
                    BlockSnapshot replacement = BlockState.fromString(replacementStateString).snapshotFor(location);

                    // see if we have extra_original or extra_replacement, then apply its data.
                    // It's not always present for storage space purposes.
                    try {
                        final String extraOriginal = rs.getString("extra_original");
                        if (extraOriginal != null) {
                            original = original.withRawData(DataFormats.JSON.get().read(extraOriginal));
                        }
                        final String extraReplacement = rs.getString("extra_replacement");
                        if (extraReplacement != null) {
                            replacement = replacement.withRawData(DataFormats.JSON.get().read(extraReplacement));
                        }
                    } catch (Exception e) {
                        Espial.getInstance().getLogger().error("Unable to get raw data for EspialBlockRecord", e);
                    }

                     future.complete(new SpongeBlockRecord(id, date, user, entityType, location, event, original, replacement, rolledBack));
                })
                .plugin(Espial.getInstance().getContainer())
                .build());
        return future;
    }

}
