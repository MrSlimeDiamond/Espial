package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.sponge.Espial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
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
    private final ServerLocation location;
    private final EspialEvent event;
    private final DataContainer extraData;

    public SpongeEspialRecord(@NotNull final Date date,
                              @NotNull final UUID user,
                              @NotNull final ServerLocation location,
                              @NotNull final EspialEvent event,
                              final boolean rolledBack,
                              @Nullable DataContainer extraData) {
        this.date = date;
        this.user = user;
        this.location = location;
        this.event = event;
        this.rolledBack = rolledBack;
        this.extraData = extraData;
    }

    public SpongeEspialRecord(final int id,
                              @NotNull final Date date,
                              @NotNull final UUID user,
                              @NotNull final ServerLocation location,
                              @NotNull final EspialEvent event,
                              final boolean rolledBack,
                              @Nullable DataContainer extraData) {
        this.id = id;
        this.date = date;
        this.user = user;
        this.location = location;
        this.event = event;
        this.rolledBack = rolledBack;
        this.extraData = extraData;
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
        return Optional.of(user);
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

    @Override
    public Optional<DataContainer> getExtraData() {
        return Optional.ofNullable(extraData);
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
