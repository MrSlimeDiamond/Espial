package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Date;
import java.util.UUID;

public class SpongeBlockRecordBuilder implements EspialBlockRecord.Builder {

    private BlockSnapshot original;
    private BlockSnapshot replacement;
    private Date date = new Date();
    private UUID user;
    private EntityType<?> entityType;
    private ServerLocation location;
    private EspialEvent event;

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
    public EspialRecord.Builder entityType(@NotNull final EntityType<?> entityType) {
        this.entityType = entityType;
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
    public EspialBlockRecord.Builder original(@NotNull final BlockSnapshot original) {
        this.original = original;
        return this;
    }

    @Override
    public EspialBlockRecord.Builder replacement(@NotNull final BlockSnapshot replacement) {
        this.replacement = replacement;
        return this;
    }

    @Override
    public @NotNull EspialRecord build() {
        return new SpongeBlockRecord(-1, date, user, entityType, location, event, original, replacement, false);
    }

}
