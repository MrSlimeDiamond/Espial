package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Date;
import java.util.UUID;

public class SpongeBlockRecord extends SpongeEspialRecord implements EspialBlockRecord {

    private final BlockState blockState;

    public SpongeBlockRecord(final int id, final Date date, final UUID user, final ServerLocation location,
                             final EspialEvent event, final BlockState blockState, final boolean rolledBack) {
        super(id, date, user, location, event, rolledBack);
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
            getLocation().setBlock(blockState);
        }
        this.setRolledBack(true);
    }

    @Override
    public void restore() {
        // TODO: Handle blocks which aren't replaced properly better
        if (getEvent().equals(EspialEvents.PLACE.get())) {
            getLocation().setBlock(blockState);
        } else {
            getLocation().setBlock(BlockTypes.AIR.get().defaultState());
        }
        this.setRolledBack(false);
    }

}
