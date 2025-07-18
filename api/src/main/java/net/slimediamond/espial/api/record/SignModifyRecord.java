package net.slimediamond.espial.api.record;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;

import java.util.List;

public interface SignModifyRecord extends EspialRecord {

    /**
     * Get the original contents of the sign
     *
     * @return Original sign contents
     */
    List<Component> getOriginalContents();

    /**
     * Get the replacement (new) contents of the sign
     *
     * @return Replacement sign contents
     */
    List<Component> getReplacementContents();

    /**
     * Get whether this record affected the front side
     *
     * @return {@code true} if front side, {@code false} if back
     */
    boolean isFrontSide();

    /**
     * Get the block state of the sign
     *
     * @return Sign block state
     */
    BlockState getBlockState();

    static Builder builder() {
        return Sponge.game().builderProvider().provide(Builder.class);
    }

    interface Builder extends EspialRecord.Builder<Builder> {

        /**
         * Set the original contents of the sign
         *
         * <p>This is required</p>
         *
         * @param originalContents The original contents of the sign
         * @return This builder, for chaining
         */
        Builder originalContents(List<Component> originalContents);

        /**
         * Set the replacement contents of the sign
         *
         * <p>This is required</p>
         *
         * @param replacementContents The new contents of the sign
         * @return This builder, for chaining
         */
        Builder replacementContents(List<Component> replacementContents);

        /**
         * Set whether the record affects the front side
         *
         * <p>{@code true} by default</p>
         *
         * @param frontSide Whether the front side is used
         * @return This builder, for chaining
         */
        Builder frontSide(boolean frontSide);

        /**
         * Set the block state of the sign
         *
         * <p>This is required</p>
         *
         * @param blockState The block state of the sign
         * @return This builder, for chaining
         */
        Builder blockState(BlockState blockState);

    }

}
