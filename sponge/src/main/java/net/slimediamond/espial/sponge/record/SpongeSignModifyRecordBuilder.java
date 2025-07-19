package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.SignText;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.record.SignModifyRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;

public class SpongeSignModifyRecordBuilder extends SpongeRecordBuilder<SignModifyRecord.Builder>
        implements SignModifyRecord.Builder {

    private SignText originalContents;
    private SignText replacementContents;
    private BlockState blockState;
    private boolean frontSide = true;

    @Override
    public SignModifyRecord.Builder originalContents(final SignText originalContents) {
        this.originalContents = originalContents;
        return this;
    }

    @Override
    public SignModifyRecord.Builder replacementContents(final SignText replacementContents) {
        this.replacementContents = replacementContents;
        return this;
    }

    @Override
    public SignModifyRecord.Builder frontSide(final boolean frontSide) {
        this.frontSide = frontSide;
        return this;
    }

    @Override
    public SignModifyRecord.Builder blockState(final BlockState blockState) {
        this.blockState = blockState;
        return this;
    }

    @Override
    public @NotNull EspialRecord build() {
        return new SpongeSignModifyRecord(date, user, entityType, location, event, false, originalContents, replacementContents, frontSide, blockState);
    }

}
