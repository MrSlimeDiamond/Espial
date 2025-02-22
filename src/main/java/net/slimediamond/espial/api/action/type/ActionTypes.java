package net.slimediamond.espial.api.action.type;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.block.transaction.Operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Action types registry
 *
 * @author Findlay Richardon (SlimeDiamond)
 */
public class ActionTypes {
    private static final List<ActionType> types = new ArrayList<>();
    private static final Map<Integer, ActionType> ids = new HashMap<>();

    public static final ActionType BREAK = ActionType.builder()
            .name("break")
            .verb("broke")
            .description("Break a block")
            .id(0)
            .build();

    public static final ActionType PLACE = ActionType.builder()
            .name("place")
            .verb("placed")
            .description("Place a block")
            .id(1)
            .build();

    public static final ActionType DECAY = ActionType.builder()
            .name("decay")
            .verb("decayed")
            .id(2)
            .build();

    public static final ActionType GROWTH = ActionType.builder()
            .name("growth")
            .verb("grew")
            .description("Grow a block")
            .id(3)
            .build();

    public static final ActionType LIQUID_DECAY = ActionType.builder()
            .name("liquid_decay")
            .verb("decayed")
            .id(4)
            .build();

    public static final ActionType LIQUID_SPREAD = ActionType.builder()
            .name("liquid_spread")
            .verb("spread")
            .id(5)
            .build();

    public static final ActionType MODIFY = ActionType.builder()
            .name("modify")
            .verb("modified")
            .description("Change a block")
            .id(6)
            .build();

    public static final ActionType INTERACT = ActionType.builder()
            .name("interact")
            .verb("used")
            .description("Interact with a block")
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

        for (ActionType type : types) {
            ids.put(type.getId(), type);
        }
    }

    private static final Map<Operation, ActionType> operations = Map.of(
            Operations.BREAK.get(), BREAK,
            Operations.PLACE.get(), PLACE,
            Operations.DECAY.get(), DECAY,
            Operations.GROWTH.get(), GROWTH,
            Operations.LIQUID_DECAY.get(), LIQUID_DECAY,
            Operations.LIQUID_SPREAD.get(), LIQUID_SPREAD,
            Operations.MODIFY.get(), MODIFY
    );

    @Nullable
    public static ActionType fromSponge(Operation operation) {
        return operations.getOrDefault(operation, null);
    }

    @Nullable
    public static ActionType fromId(int id) {
        return ids.getOrDefault(id, null);
    }
}