package net.slimediamond.espial.nbt;

import net.slimediamond.espial.StoredBlock;
import net.slimediamond.espial.nbt.json.JsonNBTData;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;

import java.util.concurrent.atomic.AtomicBoolean;

public class NBTApplier {

    public static void applyData(BlockState blockState, StoredBlock storedBlock) {
        applyData(new JsonNBTData(), blockState, storedBlock);
    }

    public static void applyData(JsonNBTData nbtData, BlockState blockState, StoredBlock storedBlock) {
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

        // Only apply the data if it's relevant,
        // so we don't take up as much storage space.
        if (applyData.get()) {
            storedBlock.setNBT(nbtData);
        }
    }
}
