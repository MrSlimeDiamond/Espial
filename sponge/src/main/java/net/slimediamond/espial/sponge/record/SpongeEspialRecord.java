package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.sponge.Espial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public abstract class SpongeEspialRecord implements EspialRecord {

    private int id = -1;
    private boolean rolledBack;
    private final Date date;
    private final UUID user;
    private final EntityType<?> entityType;
    private final ServerLocation location;
    private final EspialEvent event;

    public SpongeEspialRecord(@NotNull final Date date,
                              @Nullable final UUID user,
                              @NotNull final EntityType<?> entityType,
                              @NotNull final ServerLocation location,
                              @NotNull final EspialEvent event,
                              final boolean rolledBack) {
        this.date = date;
        this.user = user;
        this.entityType = entityType;
        this.location = location;
        this.event = event;
        this.rolledBack = rolledBack;
    }

    public SpongeEspialRecord(final int id,
                              @NotNull final Date date,
                              @Nullable final UUID user,
                              @NotNull final EntityType<?> entityType,
                              @NotNull final ServerLocation location,
                              @NotNull final EspialEvent event,
                              final boolean rolledBack) {
        this.id = id;
        this.date = date;
        this.user = user;
        this.entityType = entityType;
        this.location = location;
        this.event = event;
        this.rolledBack = rolledBack;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public Optional<UUID> getUser() {
        return Optional.ofNullable(user);
    }

    @Override
    public EntityType<?> getEntityType() {
        return entityType;
    }

    @Override
    public ServerLocation getLocation() {
        return location;
    }

    @Override
    public EspialEvent getEvent() {
        return event;
    }

    @Override
    public boolean isRolledBack() {
        return rolledBack;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public void setRolledBack(final boolean rolledBack) {
        this.rolledBack = rolledBack;
        try {
            Espial.getInstance().getDatabase().setRolledBack(this, rolledBack);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
