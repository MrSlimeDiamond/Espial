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
    private static final List<EventType> types = new ArrayList<>();
    private static final Map<Integer, EventType> ids = new HashMap<>();

    public static final EventType BREAK = EventType.builder()
            .name("break")
            .verb("broke")
            .description("Break a block")
            .action(ActionType.BLOCK)
            .id(0)
            .build();

    public static final EventType PLACE = EventType.builder()
            .name("place")
            .verb("placed")
            .description("Place a block")
            .action(ActionType.BLOCK)
            .id(1)
            .build();

    public static final EventType DECAY = EventType.builder()
            .name("decay")
            .verb("decayed")
            .action(ActionType.BLOCK)
            .id(2)
            .build();

    public static final EventType GROWTH = EventType.builder()
            .name("growth")
            .verb("grew")
            .description("Grow a block")
            .action(ActionType.BLOCK)
            .id(3)
            .build();

    public static final EventType LIQUID_DECAY = EventType.builder()
            .name("liquid_decay")
            .verb("decayed")
            .action(ActionType.BLOCK)
            .id(4)
            .build();

    public static final EventType LIQUID_SPREAD = EventType.builder()
            .name("liquid_spread")
            .verb("spread")
            .action(ActionType.BLOCK)
            .id(5)
            .build();

    public static final EventType MODIFY = EventType.builder()
            .name("modify")
            .verb("modified")
            .description("Change a block")
            .action(ActionType.BLOCK)
            .id(6)
            .build();

    public static final EventType INTERACT = EventType.builder()
            .name("interact")
            .verb("used")
            .description("Interact with a block")
            .action(ActionType.BLOCK)
            .id(8) // backwards compatibility
            .build();

    static {
        types.add(BREAK);
        types.add(PLACE);
        types.add(DECAY);
        types.add(GROWTH);
        types.add(LIQUID_DECAY);
        types.add(LIQUID_SPREAD);
        types.add(MODIFY);
        types.add(INTERACT);

        for (EventType type : types) {
            ids.put(type.getId(), type);
        }
    }

    private static final Map<Operation, EventType> operations = Map.of(
            Operations.BREAK.get(), BREAK,
            Operations.PLACE.get(), PLACE,
            Operations.DECAY.get(), DECAY,
            Operations.GROWTH.get(), GROWTH,
            Operations.LIQUID_DECAY.get(), LIQUID_DECAY,
            Operations.LIQUID_SPREAD.get(), LIQUID_SPREAD,
            Operations.MODIFY.get(), MODIFY
    );

    @Nullable
    public static EventType fromSponge(Operation operation) {
        return operations.getOrDefault(operation, null);
    }

    @Nullable
    public static EventType fromId(int id) {
        return ids.getOrDefault(id, null);
    }
}