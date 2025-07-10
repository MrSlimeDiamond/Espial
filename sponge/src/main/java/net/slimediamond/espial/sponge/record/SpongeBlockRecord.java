package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Date;
import java.util.UUID;

public class SpongeBlockRecord extends SpongeEspialRecord implements EspialBlockRecord {

    private final BlockState blockState;

    public SpongeBlockRecord(final int id,
                             @NotNull final Date date,
                             @Nullable final UUID user,
                             @NotNull final EntityType<?> entityType,
                             @NotNull final ServerLocation location,
                             @NotNull final EspialEvent event,
                             @NotNull final BlockState blockState,
                             final boolean rolledBack,
                             @Nullable final DataContainer extraData) {
        super(id, date, user, entityType, location, event, rolledBack, extraData);
        this.blockState = blockState;
    }

    @Override
    public BlockState getBlockState() {
        return blockState;
    }

    @Override
    public void rollback() {
        // TODO: Handle blocks which aren't replaced properly better
        if (getEvent().equals(EspialEvents.PLACE.get())) {
            getLocation().setBlock(BlockTypes.AIR.get().defaultState());
        } else {
            getBlockSnapshot().restore(true, BlockChangeFlags.ALL);
        }
    }

    @Override
    public void restore() {
        // TODO: Handle blocks which aren't replaced properly better (rollback/restore blocks)
        if (getEvent().equals(EspialEvents.PLACE.get())) {
            getBlockSnapshot().restore(true, BlockChangeFlags.DEFAULT_PLACEMENT);
        } else {
            getLocation().setBlock(BlockTypes.AIR.get().defaultState());
        }
    }

    @Override
    public BlockSnapshot getBlockSnapshot() {
        BlockSnapshot blockSnapshot = blockState.snapshotFor(getLocation());
        if (getExtraData().isPresent()) {
            blockSnapshot = blockSnapshot.withContainer(getExtraData().get());
        }
        return blockSnapshot;
    }

}
