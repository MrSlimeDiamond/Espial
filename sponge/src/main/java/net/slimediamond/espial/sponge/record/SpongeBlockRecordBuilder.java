package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Date;
import java.util.UUID;

public class SpongeBlockRecordBuilder implements EspialBlockRecord.Builder {

    private BlockState blockState;
    private Date date = new Date();
    private UUID user;
    private ServerLocation location;
    private EspialEvent event;
    private DataContainer extraData;

    @Override
    public EspialBlockRecord.Builder blockState(final @NotNull BlockState blockState) {
        this.blockState = blockState;
        return this;
    }

    @Override
    public EspialRecord.Builder date(final @NotNull Date date) {
        this.date = date;
        return this;
    }

    @Override
    public EspialRecord.Builder user(final @NotNull UUID user) {
        this.user = user;
        return this;
    }

    @Override
    public EspialRecord.Builder location(final @NotNull ServerLocation location) {
        this.location = location;
        return this;
    }

    @Override
    public EspialRecord.Builder event(final @NotNull EspialEvent event) {
        this.event = event;
        return this;
    }

    @Override
    public EspialBlockRecord.Builder extraData(@NotNull final DataContainer extraData) {
        this.extraData = extraData;
        return this;
    }

    @Override
    public @NotNull EspialRecord build() {
        return new SpongeBlockRecord(-1, date, user, location, event, blockState, false, extraData);
    }

}
