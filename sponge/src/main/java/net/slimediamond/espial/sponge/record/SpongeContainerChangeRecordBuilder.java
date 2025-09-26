package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.record.ContainerChangeRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class SpongeContainerChangeRecordBuilder extends SpongeRecordBuilder<ContainerChangeRecord.Builder>
        implements ContainerChangeRecord.Builder {

    private int slot;
    private ItemStackSnapshot original;
    private ItemStackSnapshot replacement;

    @Override
    public ContainerChangeRecord.Builder slot(final int slot) {
        this.slot = slot;
        return this;
    }

    @Override
    public ContainerChangeRecord.Builder original(final ItemStackSnapshot original) {
        this.original = original;
        return this;
    }

    @Override
    public ContainerChangeRecord.Builder replacement(final ItemStackSnapshot replacement) {
        this.replacement = replacement;
        return this;
    }

    @Override
    public @NotNull EspialRecord build() {
        return new SpongeContainerChangeRecord(-1, date, user, entityType, location, event, false, slot, original, replacement);
    }

}
