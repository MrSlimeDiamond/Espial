package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.record.ContainerChangeRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.entity.carrier.chest.Chest;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class SpongeContainerChangeRecord extends SpongeEspialRecord implements ContainerChangeRecord {

    private final int slot;
    private final ItemStackSnapshot original;
    private final ItemStackSnapshot replacement;

    public SpongeContainerChangeRecord(final int id,
                                       final @NotNull Date date,
                                       final @Nullable UUID user,
                                       final @NotNull EntityType<?> entityType,
                                       final @NotNull ServerLocation location,
                                       final @NotNull EspialEvent event,
                                       final boolean rolledBack,
                                       final int slot,
                                       final ItemStackSnapshot original,
                                       final ItemStackSnapshot replacement) {
        super(id, date, user, entityType, location, event, rolledBack);
        this.slot = slot;
        this.original = original;
        this.replacement = replacement;
    }

    @Override
    public int getSlot() {
        return this.slot;
    }

    @Override
    public ItemStackSnapshot getOriginal() {
        return this.original;
    }

    @Override
    public ItemStackSnapshot getReplacement() {
        return this.replacement;
    }

    @Override
    public String getTarget() {
        return this.getAffectedItem().type().key(RegistryTypes.ITEM_TYPE).formatted();
    }

    @Override
    public void rollback() {
        this.getInventory()
                .flatMap(container -> container.slot(this.slot))
                .ifPresent(slot -> slot.set(this.getOriginal()));
    }

    @Override
    public void restore() {
        this.getInventory()
                .flatMap(container -> container.slot(this.slot))
                .ifPresent(slot -> slot.set(this.getReplacement()));
    }

    private Optional<Inventory> getInventory() {
        return this.getLocation().blockEntity()
                .filter(Carrier.class::isInstance)
                .map(Carrier.class::cast)
                .map(carrier -> carrier instanceof Chest chest
                        ? chest.doubleChestInventory().orElse(chest.inventory())
                        : carrier.inventory());
    }

}
