package net.slimediamond.espial.util;

import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.nbt.NBTData;
import net.slimediamond.espial.api.nbt.json.JsonNBTData;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Applies NBT data for an Action.
 * Currently implemented with {@link JsonNBTData}
 *
 * @author SlimeDiamond
 */
public class NBTApplier {
    public static NBTData createNBTData(BlockState blockState) {
        return createNBTData(new JsonNBTData(), blockState);
    }

    public static boolean update(JsonNBTData nbtData, BlockState blockState) {
        AtomicBoolean modified = new AtomicBoolean(false);

        if (blockState.supports(Keys.DIRECTION)) {
            blockState.get(Keys.DIRECTION).ifPresent(direction -> {
                modified.set(true);
                nbtData.setDirection(direction);
            });
        }

        if (blockState.supports(Keys.IS_WATERLOGGED)) {
            blockState.get(Keys.IS_WATERLOGGED).ifPresent(waterlogged -> {
                if (waterlogged) {
                    modified.set(true);
                    nbtData.setWaterlogged(true);
                }
            });
        }

        if (blockState.supports(Keys.AXIS)) {
            blockState.get(Keys.AXIS).ifPresent(axis -> {
                modified.set(true);
                nbtData.setAxis(axis);
            });
        }

        if (blockState.supports(Keys.GROWTH_STAGE)) {
            blockState.get(Keys.GROWTH_STAGE).ifPresent(growthStage -> {
                modified.set(true);
                nbtData.setGrowthStage(growthStage);
            });
        }

        return modified.get();
    }

    public static NBTData createNBTData(JsonNBTData nbtData, BlockState blockState) {
        update(nbtData, blockState);
        return nbtData;
    }

    @Deprecated
    public static void applyData(BlockState blockState, BlockAction blockAction) {
        applyData(new JsonNBTData(), blockState, blockAction);
    }

    @Deprecated
    public static void applyData(JsonNBTData nbtData, BlockState blockState, BlockAction blockAction) {
        AtomicBoolean applyData = new AtomicBoolean(false);

        if (blockState.supports(Keys.DIRECTION)) {
            blockState.get(Keys.DIRECTION).ifPresent(direction -> {
                applyData.set(true);
                nbtData.setDirection(direction);
            });
        }

        if (blockState.supports(Keys.IS_WATERLOGGED)) {
            blockState.get(Keys.IS_WATERLOGGED).ifPresent(waterlogged -> {
                applyData.set(true);
                nbtData.setWaterlogged(waterlogged);
            });
        }

        if (blockState.supports(Keys.AXIS)) {
            blockState.get(Keys.AXIS).ifPresent(axis -> {
                applyData.set(true);
                nbtData.setAxis(axis);
            });
        }

        // Only apply the data if it's relevant,
        // so we don't take up as much storage space.
        if (applyData.get()) {
            blockAction.setNBT(nbtData);
        }
    }
}
