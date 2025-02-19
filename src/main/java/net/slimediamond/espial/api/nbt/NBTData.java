package net.slimediamond.espial.api.nbt;

import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;

import javax.annotation.Nullable;

/**
 * Stored NBT data on a block.
 */
public interface NBTData {

    /**
     * Get sign data for this block
     * @return If available, return sign data
     */
    @Nullable
    SignData getSignData();

    /**
     * Get a block's direction
     * @return Block direction
     */
    Direction getDirection();

    /**
     * Get a block's axis (x, y, or z)
     * @return Block axis
     */
    Axis getAxis();

    /**
     * Whether the block is waterlogged
     * @return Whether the block is waterlogged
     */
    boolean isWaterlogged();
}
