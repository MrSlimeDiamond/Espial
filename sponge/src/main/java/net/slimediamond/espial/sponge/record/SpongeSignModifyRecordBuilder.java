package net.slimediamond.espial.sponge.record;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.record.SignModifyRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;

import java.util.List;

public class SpongeSignModifyRecordBuilder extends SpongeRecordBuilder<SignModifyRecord.Builder>
        implements SignModifyRecord.Builder {

    private List<Component> originalContents;
    private List<Component> replacementContents;
    private BlockState blockState;
    private boolean frontSide = true;

    @Override
    public SignModifyRecord.Builder originalContents(final List<Component> originalContents) {
        this.originalContents = originalContents;
        return this;
    }

    @Override
    public SignModifyRecord.Builder replacementContents(final List<Component> replacementContents) {
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
