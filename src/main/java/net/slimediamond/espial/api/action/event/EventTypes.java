package net.slimediamond.espial.api.action.event;

import net.slimediamond.espial.api.action.ActionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.block.transaction.Operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event types registry
 *
 * @author Findlay Richardon (SlimeDiamond)
 */
public class EventTypes {
  public static final EventType BREAK =
      EventType.builder()
          .name("Break")
          .verb("broke")
          .description("Break a block")
          .action(ActionType.BLOCK)
          .id(0)
          .build();
  public static final EventType PLACE =
      EventType.builder()
          .name("Place")
          .verb("placed")
          .description("Place a block")
          .action(ActionType.BLOCK)
          .id(1)
          .build();
  public static final EventType DECAY =
      EventType.builder()
          .name("Decay")
          .verb("decayed")
          .action(ActionType.BLOCK)
          .id(2)
          .build();
  public static final EventType GROWTH =
      EventType.builder()
          .name("growth")
          .verb("grew")
          .description("Grow a block")
          .action(ActionType.BLOCK)
          .id(3)
          .build();
  public static final EventType LIQUID_DECAY =
      EventType.builder()
          .name("Liquid Decay")
          .verb("decayed")
          .action(ActionType.BLOCK)
          .id(4)
          .build();
  public static final EventType LIQUID_SPREAD =
      EventType.builder()
          .name("Liquid Spread")
          .verb("spread")
          .action(ActionType.BLOCK)
          .id(5)
          .build();
  public static final EventType MODIFY =
      EventType.builder()
          .name("Modify")
          .verb("modified")
          .description("Change a block")
          .action(ActionType.BLOCK)
          .id(6)
          .build();
  public static final EventType INTERACT =
      EventType.builder()
          .name("Interact")
          .verb("used")
          .description("Interact with a block")
          .action(ActionType.BLOCK)
          .id(8) // backwards compatibility
          .build();
  public static final EventType HANGING_DEATH =
      EventType.builder()
          .name("Hanging Death")
          .verb("killed")
          .description("Kill a hanging entity, such as an item frame")
          .action(ActionType.HANGING_DEATH)
          .id(9)
          .build();
  public static final EventType ITEM_FRAME_REMOVE =
      EventType.builder()
          .name("Item Frame Item Remove")
          .verb("removed from item frame")
          .description("A player has removed an item from an item frame by attacking it")
          .action(ActionType.ITEM_FRAME_REMOVE)
          .id(10)
          .build();

  private static final List<EventType> types = new ArrayList<>();
  private static final Map<Integer, EventType> ids = new HashMap<>();
  private static final Map<Operation, EventType> operations =
      Map.of(
          Operations.BREAK.get(), BREAK,
          Operations.PLACE.get(), PLACE,
          Operations.DECAY.get(), DECAY,
          Operations.GROWTH.get(), GROWTH,
          Operations.LIQUID_DECAY.get(), LIQUID_DECAY,
          Operations.LIQUID_SPREAD.get(), LIQUID_SPREAD,
          Operations.MODIFY.get(), MODIFY);

  static {
    types.add(BREAK);
    types.add(PLACE);
    types.add(DECAY);
    types.add(GROWTH);
    types.add(LIQUID_DECAY);
    types.add(LIQUID_SPREAD);
    types.add(MODIFY);
    types.add(INTERACT);
    types.add(HANGING_DEATH);
    types.add(ITEM_FRAME_REMOVE);

    for (EventType type : types) {
      ids.put(type.getId(), type);
    }
  }

  @Nullable
  public static EventType fromSponge(Operation operation) {
    return operations.getOrDefault(operation, null);
  }

  @Nullable
  public static EventType fromId(int id) {
    return ids.getOrDefault(id, null);
  }
}
