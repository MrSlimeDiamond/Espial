package net.slimediamond.espial.api.nbt;

import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.nbt.json.JsonNBTData;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;

import java.util.concurrent.atomic.AtomicBoolean;

public class NBTApplier {

    public static void applyData(BlockState blockState, BlockAction blockAction) {
        applyData(new JsonNBTData(), blockState, blockAction);
    }

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
