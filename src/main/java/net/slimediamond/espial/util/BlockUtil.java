package net.slimediamond.espial.util;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.function.Supplier;

/**
 * Block groups and stuff
 *
 * @author SlimeDiamond
 * @author mrhell228
 */
@SuppressWarnings("unchecked")
public class BlockUtil {
  public static final HashSet<BlockType> SIGNS =
      builder()
          .add(
              // Standing signs
              BlockTypes.ACACIA_SIGN,
              BlockTypes.BIRCH_SIGN,
              BlockTypes.DARK_OAK_SIGN,
              BlockTypes.JUNGLE_SIGN,
              BlockTypes.OAK_SIGN,
              BlockTypes.SPRUCE_SIGN,
              BlockTypes.CRIMSON_SIGN,
              BlockTypes.WARPED_SIGN,
              // Standing wall signs
              BlockTypes.ACACIA_WALL_SIGN,
              BlockTypes.BIRCH_WALL_SIGN,
              BlockTypes.DARK_OAK_WALL_SIGN,
              BlockTypes.JUNGLE_WALL_SIGN,
              BlockTypes.OAK_WALL_SIGN,
              BlockTypes.SPRUCE_WALL_SIGN,
              BlockTypes.CRIMSON_WALL_SIGN,
              BlockTypes.WARPED_WALL_SIGN)
          .add(
              // Hanging signs
              BlockTypes.ACACIA_HANGING_SIGN,
              BlockTypes.BAMBOO_HANGING_SIGN,
              BlockTypes.BIRCH_HANGING_SIGN,
              BlockTypes.CHERRY_HANGING_SIGN,
              BlockTypes.CRIMSON_HANGING_SIGN,
              BlockTypes.DARK_OAK_HANGING_SIGN,
              BlockTypes.JUNGLE_HANGING_SIGN,
              BlockTypes.OAK_HANGING_SIGN,
              BlockTypes.SPRUCE_HANGING_SIGN,
              BlockTypes.WARPED_HANGING_SIGN,
              BlockTypes.PALE_OAK_HANGING_SIGN,
              BlockTypes.MANGROVE_HANGING_SIGN,
              // Wall hanging signs
              BlockTypes.ACACIA_WALL_HANGING_SIGN,
              BlockTypes.BAMBOO_WALL_HANGING_SIGN,
              BlockTypes.BIRCH_WALL_HANGING_SIGN,
              BlockTypes.CHERRY_WALL_HANGING_SIGN,
              BlockTypes.CRIMSON_WALL_HANGING_SIGN,
              BlockTypes.DARK_OAK_WALL_HANGING_SIGN,
              BlockTypes.JUNGLE_WALL_HANGING_SIGN,
              BlockTypes.OAK_WALL_HANGING_SIGN,
              BlockTypes.SPRUCE_WALL_HANGING_SIGN,
              BlockTypes.WARPED_WALL_HANGING_SIGN,
              BlockTypes.PALE_OAK_WALL_HANGING_SIGN,
              BlockTypes.MANGROVE_WALL_HANGING_SIGN)
          .build();
  public static final HashSet<BlockType> AIR =
      builder().add(BlockTypes.AIR, BlockTypes.VOID_AIR, BlockTypes.CAVE_AIR).build();
  public static final HashSet<BlockType> PRESSURE_PLATES =
      builder()
          .add(
              BlockTypes.ACACIA_PRESSURE_PLATE,
              BlockTypes.BIRCH_PRESSURE_PLATE,
              BlockTypes.DARK_OAK_PRESSURE_PLATE,
              BlockTypes.JUNGLE_PRESSURE_PLATE,
              BlockTypes.OAK_PRESSURE_PLATE,
              BlockTypes.SPRUCE_PRESSURE_PLATE,
              BlockTypes.CRIMSON_PRESSURE_PLATE,
              BlockTypes.WARPED_PRESSURE_PLATE,
              BlockTypes.STONE_PRESSURE_PLATE,
              BlockTypes.POLISHED_BLACKSTONE_PRESSURE_PLATE,
              BlockTypes.HEAVY_WEIGHTED_PRESSURE_PLATE,
              BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE)
          .build();
  private static final HashSet<BlockType> SAPLINGS =
      builder()
          .add(
              BlockTypes.ACACIA_SAPLING,
              BlockTypes.BIRCH_SAPLING,
              BlockTypes.DARK_OAK_SAPLING,
              BlockTypes.JUNGLE_SAPLING,
              BlockTypes.OAK_SAPLING,
              BlockTypes.SPRUCE_SAPLING)
          .add(BlockTypes.BAMBOO_SAPLING)
          .build();
  private static final HashSet<BlockType> LEAVES =
      builder()
          .add(
              BlockTypes.ACACIA_LEAVES,
              BlockTypes.BIRCH_LEAVES,
              BlockTypes.DARK_OAK_LEAVES,
              BlockTypes.JUNGLE_LEAVES,
              BlockTypes.OAK_LEAVES,
              BlockTypes.SPRUCE_LEAVES)
          .build();
  private static final HashSet<BlockType> FENCES =
      builder()
          .add(
              BlockTypes.ACACIA_FENCE,
              BlockTypes.BIRCH_FENCE,
              BlockTypes.DARK_OAK_FENCE,
              BlockTypes.JUNGLE_FENCE,
              BlockTypes.OAK_FENCE,
              BlockTypes.SPRUCE_FENCE)
          .add(BlockTypes.CRIMSON_FENCE, BlockTypes.WARPED_FENCE)
          .add(BlockTypes.NETHER_BRICK_FENCE)
          .build();
  private static final HashSet<BlockType> FENCE_GATES =
      builder()
          .add(
              BlockTypes.ACACIA_FENCE_GATE,
              BlockTypes.BIRCH_FENCE_GATE,
              BlockTypes.DARK_OAK_FENCE_GATE,
              BlockTypes.JUNGLE_FENCE_GATE,
              BlockTypes.OAK_FENCE_GATE,
              BlockTypes.SPRUCE_FENCE_GATE)
          .add(BlockTypes.CRIMSON_FENCE_GATE, BlockTypes.WARPED_FENCE_GATE)
          .build();
  private static final HashSet<BlockType> DOORS =
      builder()
          .add(
              BlockTypes.ACACIA_DOOR,
              BlockTypes.BIRCH_DOOR,
              BlockTypes.DARK_OAK_DOOR,
              BlockTypes.JUNGLE_DOOR,
              BlockTypes.OAK_DOOR,
              BlockTypes.SPRUCE_DOOR)
          .add(BlockTypes.CRIMSON_DOOR, BlockTypes.WARPED_DOOR)
          .add(BlockTypes.IRON_DOOR)
          .build();
  private static final HashSet<BlockType> TRAPDOORS =
      builder()
          .add(
              BlockTypes.ACACIA_TRAPDOOR,
              BlockTypes.BIRCH_TRAPDOOR,
              BlockTypes.DARK_OAK_TRAPDOOR,
              BlockTypes.JUNGLE_TRAPDOOR,
              BlockTypes.OAK_TRAPDOOR,
              BlockTypes.SPRUCE_TRAPDOOR)
          .add(BlockTypes.CRIMSON_TRAPDOOR, BlockTypes.WARPED_TRAPDOOR)
          .add(BlockTypes.IRON_TRAPDOOR)
          .build();
  private static final HashSet<BlockType> BUTTONS =
      builder()
          .add(
              BlockTypes.ACACIA_BUTTON,
              BlockTypes.BIRCH_BUTTON,
              BlockTypes.DARK_OAK_BUTTON,
              BlockTypes.JUNGLE_BUTTON,
              BlockTypes.OAK_BUTTON,
              BlockTypes.SPRUCE_BUTTON)
          .add(BlockTypes.CRIMSON_BUTTON, BlockTypes.WARPED_BUTTON)
          .add(BlockTypes.POLISHED_BLACKSTONE_BUTTON, BlockTypes.STONE_BUTTON)
          .build();
  private static final HashSet<BlockType> WALL_SIGNS =
      builder()
          .add(
              BlockTypes.ACACIA_WALL_SIGN,
              BlockTypes.BIRCH_WALL_SIGN,
              BlockTypes.DARK_OAK_WALL_SIGN,
              BlockTypes.JUNGLE_WALL_SIGN,
              BlockTypes.OAK_WALL_SIGN,
              BlockTypes.SPRUCE_WALL_SIGN)
          .add(BlockTypes.CRIMSON_WALL_SIGN, BlockTypes.WARPED_WALL_SIGN)
          .build();
  private static final HashSet<BlockType> GLASS_PANES =
      builder()
          .add(
              BlockTypes.GLASS_PANE,
              BlockTypes.IRON_BARS,
              BlockTypes.BLACK_STAINED_GLASS_PANE,
              BlockTypes.BLUE_STAINED_GLASS_PANE,
              BlockTypes.BROWN_STAINED_GLASS_PANE,
              BlockTypes.CYAN_STAINED_GLASS_PANE,
              BlockTypes.GRAY_STAINED_GLASS_PANE,
              BlockTypes.GREEN_STAINED_GLASS_PANE,
              BlockTypes.LIGHT_BLUE_STAINED_GLASS_PANE,
              BlockTypes.LIGHT_GRAY_STAINED_GLASS_PANE,
              BlockTypes.LIME_STAINED_GLASS_PANE,
              BlockTypes.MAGENTA_STAINED_GLASS_PANE,
              BlockTypes.ORANGE_STAINED_GLASS_PANE,
              BlockTypes.PINK_STAINED_GLASS_PANE,
              BlockTypes.PURPLE_STAINED_GLASS_PANE,
              BlockTypes.RED_STAINED_GLASS_PANE,
              BlockTypes.WHITE_STAINED_GLASS_PANE,
              BlockTypes.YELLOW_STAINED_GLASS_PANE)
          .build();
  private static final HashSet<BlockType> WALLS =
      builder()
          .add(
              BlockTypes.ANDESITE_WALL,
              BlockTypes.DIORITE_WALL,
              BlockTypes.GRANITE_WALL,
              BlockTypes.BRICK_WALL,
              BlockTypes.COBBLESTONE_WALL,
              BlockTypes.STONE_BRICK_WALL,
              BlockTypes.MOSSY_COBBLESTONE_WALL,
              BlockTypes.MOSSY_STONE_BRICK_WALL,
              BlockTypes.BLACKSTONE_WALL,
              BlockTypes.POLISHED_BLACKSTONE_WALL,
              BlockTypes.POLISHED_BLACKSTONE_BRICK_WALL,
              BlockTypes.PRISMARINE_WALL,
              BlockTypes.NETHER_BRICK_WALL,
              BlockTypes.RED_NETHER_BRICK_WALL,
              BlockTypes.END_STONE_BRICK_WALL,
              BlockTypes.RED_SANDSTONE_WALL,
              BlockTypes.SANDSTONE_WALL)
          .build();
  private static final HashSet<BlockType> STAIRS =
      builder()
          .add(
              BlockTypes.ACACIA_STAIRS,
              BlockTypes.BIRCH_STAIRS,
              BlockTypes.DARK_OAK_STAIRS,
              BlockTypes.JUNGLE_STAIRS,
              BlockTypes.OAK_STAIRS,
              BlockTypes.SPRUCE_STAIRS)
          .add(BlockTypes.CRIMSON_STAIRS, BlockTypes.WARPED_STAIRS)
          .add(BlockTypes.ANDESITE_STAIRS, BlockTypes.DIORITE_STAIRS, BlockTypes.GRANITE_STAIRS)
          .add(
              BlockTypes.POLISHED_ANDESITE_STAIRS,
              BlockTypes.POLISHED_DIORITE_STAIRS,
              BlockTypes.GRANITE_STAIRS)
          .add(
              BlockTypes.BRICK_STAIRS,
              BlockTypes.COBBLESTONE_STAIRS,
              BlockTypes.STONE_BRICK_STAIRS,
              BlockTypes.MOSSY_COBBLESTONE_STAIRS,
              BlockTypes.MOSSY_STONE_BRICK_STAIRS,
              BlockTypes.BLACKSTONE_STAIRS,
              BlockTypes.POLISHED_BLACKSTONE_STAIRS,
              BlockTypes.POLISHED_BLACKSTONE_BRICK_STAIRS,
              BlockTypes.PRISMARINE_STAIRS,
              BlockTypes.PRISMARINE_BRICK_STAIRS,
              BlockTypes.DARK_PRISMARINE_STAIRS,
              BlockTypes.NETHER_BRICK_STAIRS,
              BlockTypes.RED_NETHER_BRICK_STAIRS,
              BlockTypes.QUARTZ_STAIRS,
              BlockTypes.SMOOTH_QUARTZ_STAIRS,
              BlockTypes.END_STONE_BRICK_STAIRS,
              BlockTypes.PURPUR_STAIRS,
              BlockTypes.RED_SANDSTONE_STAIRS,
              BlockTypes.SANDSTONE_STAIRS,
              BlockTypes.SMOOTH_RED_SANDSTONE_STAIRS,
              BlockTypes.SMOOTH_SANDSTONE_STAIRS)
          .build();
  private static final HashSet<BlockType> SHULKERS =
      builder()
          .add(
              BlockTypes.SHULKER_BOX,
              BlockTypes.BLACK_SHULKER_BOX,
              BlockTypes.BLUE_SHULKER_BOX,
              BlockTypes.BROWN_SHULKER_BOX,
              BlockTypes.CYAN_SHULKER_BOX,
              BlockTypes.GRAY_SHULKER_BOX,
              BlockTypes.GREEN_SHULKER_BOX,
              BlockTypes.LIGHT_BLUE_SHULKER_BOX,
              BlockTypes.LIGHT_GRAY_SHULKER_BOX,
              BlockTypes.LIME_SHULKER_BOX,
              BlockTypes.MAGENTA_SHULKER_BOX,
              BlockTypes.ORANGE_SHULKER_BOX,
              BlockTypes.PINK_SHULKER_BOX,
              BlockTypes.PURPLE_SHULKER_BOX,
              BlockTypes.RED_SHULKER_BOX,
              BlockTypes.WHITE_SHULKER_BOX,
              BlockTypes.YELLOW_SHULKER_BOX)
          .build();
  private static final HashSet<BlockType> FLOWER_POTS =
      builder()
          .add(
              BlockTypes.FLOWER_POT,
              BlockTypes.POTTED_BROWN_MUSHROOM,
              BlockTypes.POTTED_RED_MUSHROOM,
              BlockTypes.POTTED_ACACIA_SAPLING,
              BlockTypes.POTTED_BIRCH_SAPLING,
              BlockTypes.POTTED_DARK_OAK_SAPLING,
              BlockTypes.POTTED_JUNGLE_SAPLING,
              BlockTypes.POTTED_OAK_SAPLING,
              BlockTypes.POTTED_SPRUCE_SAPLING,
              BlockTypes.POTTED_BAMBOO,
              BlockTypes.POTTED_CACTUS,
              BlockTypes.POTTED_ORANGE_TULIP,
              BlockTypes.POTTED_PINK_TULIP,
              BlockTypes.POTTED_RED_TULIP,
              BlockTypes.POTTED_WHITE_TULIP,
              BlockTypes.POTTED_ALLIUM,
              BlockTypes.POTTED_AZURE_BLUET,
              BlockTypes.POTTED_BLUE_ORCHID,
              BlockTypes.POTTED_CORNFLOWER,
              BlockTypes.POTTED_LILY_OF_THE_VALLEY,
              BlockTypes.POTTED_OXEYE_DAISY,
              BlockTypes.POTTED_DANDELION,
              BlockTypes.POTTED_POPPY,
              BlockTypes.POTTED_WITHER_ROSE,
              BlockTypes.POTTED_DEAD_BUSH,
              BlockTypes.POTTED_FERN,
              BlockTypes.POTTED_CRIMSON_FUNGUS,
              BlockTypes.POTTED_CRIMSON_ROOTS,
              BlockTypes.POTTED_WARPED_FUNGUS,
              BlockTypes.POTTED_WARPED_ROOTS)
          .build();
  public static final HashSet<BlockType> CONTAINERS =
      builder()
          .add(SHULKERS)
          .add(FLOWER_POTS)
          .add(
              BlockTypes.CHEST,
              BlockTypes.FURNACE,
              BlockTypes.TRAPPED_CHEST,
              BlockTypes.HOPPER,
              BlockTypes.DISPENSER,
              BlockTypes.DROPPER,
              BlockTypes.JUKEBOX,
              BlockTypes.BARREL,
              BlockTypes.SMOKER,
              BlockTypes.BLAST_FURNACE,
              BlockTypes.LECTERN)
          .build();
  private static final HashSet<BlockType> ANVILS =
      builder().add(BlockTypes.ANVIL, BlockTypes.CHIPPED_ANVIL, BlockTypes.DAMAGED_ANVIL).build();
  public static final HashSet<BlockType> INTERACTIVE =
      builder()
          .add(BUTTONS)
          .add(TRAPDOORS)
          .remove(BlockTypes.IRON_TRAPDOOR)
          .add(DOORS)
          .remove(BlockTypes.IRON_DOOR)
          .add(FENCES)
          .add(FENCE_GATES)
          .add(ANVILS)
          .add(SIGNS)
          .add(
              BlockTypes.ENDER_CHEST,
              BlockTypes.ENCHANTING_TABLE,
              BlockTypes.LOOM,
              BlockTypes.SMITHING_TABLE,
              BlockTypes.GRINDSTONE,
              BlockTypes.END_PORTAL_FRAME,
              BlockTypes.RESPAWN_ANCHOR,
              BlockTypes.LODESTONE,
              BlockTypes.LOOM,
              BlockTypes.CARTOGRAPHY_TABLE,
              BlockTypes.STONECUTTER,
              BlockTypes.REPEATER,
              BlockTypes.COMPARATOR,
              BlockTypes.LEVER,
              BlockTypes.DAYLIGHT_DETECTOR,
              BlockTypes.NOTE_BLOCK,
              BlockTypes.CARTOGRAPHY_TABLE,
              BlockTypes.STONECUTTER,
              BlockTypes.BELL,
              BlockTypes.CAKE,
              BlockTypes.BEACON,
              BlockTypes.BEEHIVE,
              BlockTypes.BEE_NEST,
              BlockTypes.BREWING_STAND,
              BlockTypes.COMMAND_BLOCK,
              BlockTypes.SPAWNER)
          .build();
  private static final HashSet<BlockType> FALLING_BLOCKS =
      builder().add(ANVILS).add(BlockTypes.SAND, BlockTypes.GRAVEL).build();
  private static final HashSet<BlockType> LIQUIDS =
      builder().add(BlockTypes.WATER, BlockTypes.LAVA).build();
  public static final HashSet<BlockType> MODIFIABLE =
      builder()
          .add(SIGNS)
          .add(BlockTypes.END_PORTAL_FRAME)
          .add(BlockTypes.CAKE)
          .add(BlockTypes.DAYLIGHT_DETECTOR)
          .add(BlockTypes.NOTE_BLOCK)
          .build();

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final HashSet<BlockType> set = new HashSet<>();

    private Builder() {}

    public Builder add(BlockType... blocks) {
      Collections.addAll(this.set, blocks);
      return this;
    }

    public Builder add(Supplier<BlockType>... blocks) {
      for (Supplier<BlockType> block : blocks) {
        this.set.add(block.get());
      }
      return this;
    }

    public Builder add(Collection<BlockType> blocks) {
      this.set.addAll(blocks);
      return this;
    }

    public Builder remove(Supplier<BlockType>... blocks) {
      for (Supplier<BlockType> block : blocks) {
        this.set.remove(block.get());
      }
      return this;
    }

    public HashSet<BlockType> build() {
      return new HashSet<>(this.set);
    }
  }
}
