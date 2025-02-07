package net.slimediamond.espial;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

import java.util.ArrayList;

public class IgnoredBlocks {
    public static ArrayList<BlockType> ignoredBlocks = new ArrayList<>();

    static {
        ignoredBlocks.add(BlockTypes.AIR.get());
        ignoredBlocks.add(BlockTypes.VOID_AIR.get());
        ignoredBlocks.add(BlockTypes.CAVE_AIR.get());
    }
}
