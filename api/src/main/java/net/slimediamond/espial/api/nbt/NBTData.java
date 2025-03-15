package net.slimediamond.espial.api.nbt;

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
  Direction getDirection();

  /**
   * Get a block's growth stage
   *
   * @return Growth stage
   */
  @Nullable
  Integer getGrowthStage();

  /**
   * Get the block which we should rollback to,
   * in instances that a place action wasn't
   * modifying air. Will be null if it's air or
   * if it's anything but a place action.
   *
   * @return Block to rollback to
   */
  @Nullable
  String getRollbackBlock();

  /**
   * Get a block's axis (x, y, or z)
   *
   * @return Block axis
   */
  Axis getAxis();

  /**
   * Whether the block is waterlogged
   *
   * @return Whether the block is waterlogged
   */
  boolean isWaterlogged();
}
