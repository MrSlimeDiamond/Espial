package net.slimediamond.espial.util;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;

public final class SpongeUtil {
  /** Static only class */
  private SpongeUtil() {}

  /**
   * Get a {@link ResourceKey} from a String
   *
   * @param string String input
   * @return Resource key
   */
  public static ResourceKey getResourceKey(String string) {
    return ResourceKey.of(
        string.split(String.valueOf(ResourceKey.DEFAULT_SEPARATOR))[0],
        string.split(String.valueOf(ResourceKey.DEFAULT_SEPARATOR))[1]);
  }

  /**
   * Get an {@link Optional} of a {@link ServerWorld} from its {@link ResourceKey}
   *
   * @param resourceKey Resource key string
   * @return {@link Optional} of a {@link ServerWorld}
   */
  public static Optional<ServerWorld> getWorld(String resourceKey) {
      return Sponge.server().worldManager().world(getResourceKey(resourceKey));
  }

  /**
   * Get a block's String ID from a {@link BlockType}
   *
   * @param blockType Block type
   * @return ID such as minecraft:oak_planks
   */
  public static String getBlockId(BlockType blockType) {
    return BlockTypes.registry().valueKey(blockType).formatted();
  }
}
