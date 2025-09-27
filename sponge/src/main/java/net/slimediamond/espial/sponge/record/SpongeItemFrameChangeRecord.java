package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.ItemFrameChangeRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.*;

public class SpongeItemFrameChangeRecord extends SpongeEspialRecord implements ItemFrameChangeRecord {

    private final ItemStackSnapshot original;
    private final ItemStackSnapshot replacement;

    public SpongeItemFrameChangeRecord(final int id,
                                       final @NotNull Date date,
                                       final @Nullable UUID user,
                                       final @NotNull EntityType<?> entityType,
                                       final @NotNull ServerLocation location,
                                       final @NotNull EspialEvent event,
                                       final boolean rolledBack,
                                       final ItemStackSnapshot original,
                                       final ItemStackSnapshot replacement) {
        super(id, date, user, entityType, location, event, rolledBack);
        this.original = original;
        this.replacement = replacement;
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
        this.getItemFrame().ifPresent(itemFrame ->
                itemFrame.offer(Keys.ITEM_STACK_SNAPSHOT, this.original));
    }

    @Override
    public void restore() {
        this.getItemFrame().ifPresent(itemFrame ->
                itemFrame.offer(Keys.ITEM_STACK_SNAPSHOT, this.replacement));
    }

    private Optional<ItemFrame> getItemFrame() {
        final List<ItemFrame> entities = this.getLocation().world().nearbyEntities(this.getLocation().position(), 2)
                .stream().filter(entity -> entity instanceof ItemFrame)
                .map(entity -> (ItemFrame)entity)
                .toList();
        if (entities.isEmpty()) {
            // create item frame entity at that location
            return EntityArchetype.builder()
                    .type(EntityTypes.ITEM_FRAME.get())
                    .build()
                    .apply(this.getLocation())
                    .map(entity -> (ItemFrame) entity);
        } else {
            return Optional.of(entities.getFirst());
        }
    }

}
