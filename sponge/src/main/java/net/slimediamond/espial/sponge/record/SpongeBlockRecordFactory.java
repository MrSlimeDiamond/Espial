package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.sponge.Espial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public final class SpongeBlockRecordFactory implements RecordFactory<EspialBlockRecord> {

    @Override
    public EspialBlockRecord create(@NotNull final EspialEvent event,
                                    @NotNull final ResultSet rs,
                                    final int id,
                                    @NotNull final Date date,
                                    @Nullable final UUID user,
                                    @NotNull final EntityType<?> entityType,
                                    @NotNull final ServerLocation location,
                                    final boolean rolledBack)
            throws SQLException {
        final ResourceKey worldKey = location.worldKey();
        final BlockState originalState = BlockState.fromString(rs.getString("state_original"));
        final BlockState replacementState = BlockState.fromString(rs.getString("state_replacement"));

        BlockSnapshot original = BlockSnapshot.builder()
                .blockState(originalState)
                .world(Sponge.server().worldManager().world(worldKey).orElseThrow().properties())
                .position(location.blockPosition())
                .build();

        BlockSnapshot replacement = BlockSnapshot.builder()
                .blockState(replacementState)
                .world(Sponge.server().worldManager().world(worldKey).orElseThrow().properties())
                .position(location.blockPosition())
                .build();

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

        return new SpongeBlockRecord(id, date, user, entityType, location, event, original, replacement, rolledBack);
    }

}
