package net.slimediamond.espial.api.record;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.EntityType;

import java.util.Optional;

public interface HangingDeathRecord extends EspialRecord {

    /**
     * Get the killed entity type
     *
     * @return Killed entity type
     */
    EntityType<?> getTargetEntityType();

    /**
     * Get the extra data which should be applied to the entity type
     *
     * @return Extra data
     */
    Optional<DataContainer> getExtraData();

    static Builder builder() {
        return Sponge.game().builderProvider().provide(Builder.class);
    }

    interface Builder extends EspialRecord.Builder<Builder> {

        /**
         * Set the targeted entity type which this hanging death
         * killed
         *
         * @param entityType The entity type
         * @return This builder, for chaining
         */
        Builder targetEntityType(EntityType<?> entityType);

        /**
         * Set the extra data to be applied to the entity type
         * when this record is rolled back
         *
         * @param extraData The extra data
         * @return This builder, for chaining
         */
        Builder extraData(DataContainer extraData);

    }

}
