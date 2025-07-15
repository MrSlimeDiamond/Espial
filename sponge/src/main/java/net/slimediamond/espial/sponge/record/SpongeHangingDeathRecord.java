package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialHangingDeathRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class SpongeHangingDeathRecord extends SpongeEspialRecord implements EspialHangingDeathRecord {

    private final EntityType<?> targetEntityType;
    private final DataContainer extraData;

    public SpongeHangingDeathRecord(final @NotNull Date date, final @Nullable UUID user, final @NotNull EntityType<?> entityType,
                                    final @NotNull ServerLocation location, final @NotNull EspialEvent event, final boolean rolledBack,
                                    final EntityType<?> targetEntityType, final DataContainer extraData) {
        super(date, user, entityType, location, event, rolledBack);
        this.targetEntityType = targetEntityType;
        this.extraData = extraData;
    }

    public SpongeHangingDeathRecord(final int id, final @NotNull Date date, final @Nullable UUID user, final @NotNull EntityType<?> entityType,
                                    final @NotNull ServerLocation location, final @NotNull EspialEvent event, final boolean rolledBack,
                                    final EntityType<?> targetEntityType, final DataContainer extraData) {
        super(id, date, user, entityType, location, event, rolledBack);
        this.targetEntityType = targetEntityType;
        this.extraData = extraData;
    }

    @Override
    public EntityType<?> getTargetEntityType() {
        return targetEntityType;
    }

    @Override
    public Optional<DataContainer> getExtraData() {
        return Optional.ofNullable(extraData);
    }

    @Override
    public String getTarget() {
        return targetEntityType.key(RegistryTypes.ENTITY_TYPE).formatted();
    }

    @Override
    public void rollback() {
        // this is a death action! Restore the original entity type
        final EntityArchetype entityArchetype = getLocation()
                .createEntity(targetEntityType)
                .createArchetype();
        getExtraData().ifPresent(entityArchetype::setRawData);
        if (entityArchetype.apply(getLocation()).isEmpty()) {
            // TODO: Return some error status of some kind
        }
    }

    @Override
    public void restore() {
        // kill entity at that location
        getLocation().world().nearbyEntities(getLocation().position(), 2).stream()
                .filter(entity -> entity.type().equals(targetEntityType))
                .forEach(Entity::remove);

    }

}
