package net.slimediamond.espial.api.nbt;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;

import javax.annotation.Nullable;

/**
 * Stored NBT data on a record.
 *
 * @author SlimeDiamond
 */
public interface NBTData {

    /**
     * Get sign data for this record
     *
     * @return Sign data
     */
    @Nullable
    SignData getSignData();

    /**
     * Get a block's direction
     *
     * @return Block direction
     */
    @Nullable
    @Deprecated(since = "2.0")
    Direction getDirection();

    /**
     * Get a block's growth stage
     *
     * @return Growth stage
     */
    @Nullable
    @Deprecated(since = "2.0")
    Integer getGrowthStage();

    /**
     * Get the block which we should roll back to. This may
     * be null in cases where it is the default block
     * state of the associated {@link net.slimediamond.espial.api.action.BlockAction}
     *
     * @return Block to rollback to
     */
    @Nullable
    BlockState getRollbackBlock();

    /**
     * Get the block which we should restore to. This may
     * be null in cases where it is air.
     *
     * @return Block to restore to
     */
    @Nullable
    BlockState getRestoreBlock();

    /**
     * Get a block's axis (x, y, or z)
     *
     * @return Block axis
     */
    @Nullable
    @Deprecated(since = "2.0")
    Axis getAxis();

    /**
     * Get which half of the block was changed.
     * Null if not applicable.
     *
     * @return Block half
     */
    @Nullable
    @Deprecated(since = "2.0")
    PortionType getHalf();


    /**
     * Whether the block is waterlogged
     *
     * @return Whether the block is waterlogged
     */
    @Nullable
    @Deprecated(since = "2.0")
    Boolean isWaterlogged();
}
