package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.record.BlockRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockSnapshot;

public class SpongeBlockRecordBuilder extends SpongeRecordBuilder<BlockRecord.Builder>
        implements BlockRecord.Builder {

    private BlockSnapshot original;
    private BlockSnapshot replacement;

    @Override
    public BlockRecord.Builder original(@NotNull final BlockSnapshot original) {
        this.original = original;
        return this;
    }

    @Override
    public BlockRecord.Builder replacement(@NotNull final BlockSnapshot replacement) {
        this.replacement = replacement;
        return this;
    }

    @Override
    public @NotNull BlockRecord build() {
        return new SpongeBlockRecord(-1, date, user, entityType, location, event, original, replacement, false);
    }

}
