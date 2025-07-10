package net.slimediamond.espial.api.record;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;


public interface EspialBlockRecord extends EspialRecord {

    /**
     * Get the block state for the record
     *
     * @return Block state
     */
    BlockSnapshot getOriginalBlock();

    /**
     * Get the block snapshot for the record
     *
     * @return Block snapshot
     */
    BlockSnapshot getReplacementBlock();

    static Builder builder() {
        return Sponge.game().builderProvider().provide(Builder.class);
    }

    interface Builder extends EspialRecord.Builder {

        /**
         * Set the block snapshot for the original
         *
         * <p><strong>This is required</strong></p>
         *
         * @param original The block snapshot
         * @return This builder, for chaining
         */
        Builder original(@NotNull BlockSnapshot original);

        /**
         * Set the block snapshot for the replacement
         *
         * <p><strong>This is required</strong></p>
         *
         * @param replacement The block snapshot
         * @return This builder, for chaining
         */
        Builder replacement(@NotNull BlockSnapshot replacement);

    }

}
