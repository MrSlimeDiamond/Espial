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
            .setName("break")
            .setVerb("broke")
            .setDescription("Break a block")
            .setId(0)
            .build();

    public static final ActionType PLACE = ActionType.builder()
            .setName("place")
            .setVerb("placed")
            .setDescription("Place a block")
            .setId(1)
            .build();

    public static final ActionType DECAY = ActionType.builder()
            .setName("decay")
            .setVerb("decayed")
            .setId(2)
            .build();

    public static final ActionType GROWTH = ActionType.builder()
            .setName("growth")
            .setVerb("grew")
            .setDescription("Grow a block")
            .setId(3)
            .build();

    public static final ActionType LIQUID_DECAY = ActionType.builder()
            .setName("liquid_decay")
            .setVerb("decayed")
            .setId(4)
            .build();

    public static final ActionType LIQUID_SPREAD = ActionType.builder()
            .setName("liquid_spread")
            .setVerb("spread")
            .setId(5)
            .build();

    public static final ActionType MODIFY = ActionType.builder()
            .setName("modify")
            .setVerb("modified")
            .setDescription("Change a block")
            .setId(6)
            .build();

    public static final ActionType INTERACT = ActionType.builder()
            .setName("interact")
            .setVerb("used")
            .setDescription("Interact with a block")
            .setId(8) // backwards compatibility
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