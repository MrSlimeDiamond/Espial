package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.record.EspialBlockRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockSnapshot;

public class SpongeBlockRecordBuilder extends SpongeRecordBuilder<EspialBlockRecord, EspialBlockRecord.Builder>
        implements EspialBlockRecord.Builder {

    private BlockSnapshot original;
    private BlockSnapshot replacement;

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
    public @NotNull EspialBlockRecord build() {
        return new SpongeBlockRecord(-1, date, user, entityType, location, event, original, replacement, false);
    }

}
