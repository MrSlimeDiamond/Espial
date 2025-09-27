package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.record.ItemFrameChangeRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class SpongeItemFrameChangeRecordBuilder extends SpongeRecordBuilder<ItemFrameChangeRecord.Builder>
        implements ItemFrameChangeRecord.Builder {

    private ItemStackSnapshot original;
    private ItemStackSnapshot replacement;

    @Override
    public ItemFrameChangeRecord.Builder original(final ItemStackSnapshot original) {
        this.original = original;
        return this;
    }

    @Override
    public ItemFrameChangeRecord.Builder replacement(final ItemStackSnapshot replacement) {
        this.replacement = replacement;
        return this;
    }

    @Override
    public @NotNull EspialRecord build() {
        return new SpongeItemFrameChangeRecord(-1, date, user, entityType, location, event, false, original, replacement);
    }

}
