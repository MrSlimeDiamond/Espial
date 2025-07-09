package net.slimediamond.espial.api.record;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;

public interface EspialBlockRecord extends EspialRecord {

    /**
     * Get the block state for the record
     *
     * @return Block state
     */
    BlockState getBlockState();

    static Builder builder() {
        return Sponge.game().builderProvider().provide(Builder.class);
    }

    interface Builder extends EspialRecord.Builder {

        /**
         * Set the block state
         *
         * <p><strong>This is required</strong></p>
         *
         * @param blockState The block state
         * @return This builder, for chaining
         */
        Builder blockState(@NotNull BlockState blockState);

    }

}
