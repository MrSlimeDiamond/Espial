package net.slimediamond.espial.nbt;

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
     * Get a block's rotation value
     * @return Block rotation
     */
    int getRotation();

    /**
     * Whether the block is waterlogged
     * @return Whether the block is waterlogged
     */
    boolean isWaterlogged();
}
