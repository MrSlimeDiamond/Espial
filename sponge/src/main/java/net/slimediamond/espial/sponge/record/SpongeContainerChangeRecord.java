package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.ContainerChangeRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Date;
import java.util.UUID;

public class SpongeContainerChangeRecord extends SpongeEspialRecord implements ContainerChangeRecord {

    private final int slot;
    private final ItemStackSnapshot item;

    public SpongeContainerChangeRecord(final @NotNull Date date,
                                       final @Nullable UUID user,
                                       final @NotNull EntityType<?> entityType,
                                       final @NotNull ServerLocation location,
                                       final @NotNull EspialEvent event,
                                       final boolean rolledBack,
                                       final int slot,
                                       final ItemStackSnapshot item) {
        super(date, user, entityType, location, event, rolledBack);
        this.slot = slot;
        this.item = item;
    }

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public ItemStackSnapshot getItem() {
        return this.item;
    }

    @Override
    public String getTarget() {
        return this.item.type().key(RegistryTypes.ITEM_TYPE).formatted();
    }

    @Override
    public void rollback() {

    }

    @Override
    public void restore() {

    }

}
