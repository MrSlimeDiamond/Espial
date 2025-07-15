package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Date;
import java.util.UUID;

@SuppressWarnings("unchecked")
public abstract class SpongeRecordBuilder<T extends EspialRecord, B extends EspialRecord.Builder<B>>
        implements EspialRecord.Builder<B> {

    protected Date date = new Date();
    protected UUID user;
    protected EntityType<?> entityType;
    protected ServerLocation location;
    protected EspialEvent event;

    @Override
    public B date(@NotNull final Date date) {
        this.date = date;
        return (B)this;
    }

    @Override
    public B user(@NotNull final UUID user) {
        this.user = user;
        return (B)this;
    }

    @Override
    public B entityType(@NotNull final EntityType<?> entityType) {
        this.entityType = entityType;
        return (B)this;
    }

    @Override
    public B location(@NotNull final ServerLocation location) {
        this.location = location;
        return (B)this;
    }

    @Override
    public B event(@NotNull final EspialEvent event) {
        this.event = event;
        return (B)this;
    }

}
