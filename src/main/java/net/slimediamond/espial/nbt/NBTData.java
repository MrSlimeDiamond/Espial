package net.slimediamond.espial.nbt;

import org.spongepowered.api.util.Direction;

import javax.annotation.Nullable;
import java.util.Optional;

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
     * Whether the block is waterlogged
     * @return Whether the block is waterlogged
     */
    boolean isWaterlogged();
}
