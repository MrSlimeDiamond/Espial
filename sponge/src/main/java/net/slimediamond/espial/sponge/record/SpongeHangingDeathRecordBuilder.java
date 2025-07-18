package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.record.HangingDeathRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.EntityType;

public class SpongeHangingDeathRecordBuilder
        extends SpongeRecordBuilder<HangingDeathRecord.Builder>
        implements HangingDeathRecord.Builder {

    private EntityType<?> targetEntityType;
    private DataContainer extraData;

    @Override
    public HangingDeathRecord.Builder targetEntityType(final EntityType<?> entityType) {
        this.targetEntityType = entityType;
        return this;
    }

    @Override
    public HangingDeathRecord.Builder extraData(final DataContainer extraData) {
        this.extraData = extraData;
        return this;
    }

    @Override
    public @NotNull HangingDeathRecord build() {
        return new SpongeHangingDeathRecord(date, user, entityType, location, event, false, targetEntityType, extraData);
    }

}
