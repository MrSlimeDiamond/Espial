package net.slimediamond.espial.api.action;

import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.block.transaction.Operations;

import java.util.HashMap;
import java.util.Map;

public enum ActionType {
    BREAK(0),
    PLACE(1),
    DECAY(2),
    GROWTH(3),
    LIQUID_DECAY(4),
    LIQUID_SPREAD(5),
    MODIFY(6),
    UNKNOWN(7),
    INTERACT(8);

    private static final Map<Operation, ActionType> operations = Map.of(
            Operations.BREAK.get(), BREAK,
            Operations.PLACE.get(), PLACE,
            Operations.DECAY.get(), DECAY,
            Operations.GROWTH.get(), GROWTH,
            Operations.LIQUID_DECAY.get(), LIQUID_DECAY,
            Operations.LIQUID_SPREAD.get(), LIQUID_SPREAD,
            Operations.MODIFY.get(), MODIFY
    );

    private static final Map<Integer, ActionType> ids = new HashMap<>();

    static {
        for (ActionType type : values()) {
            ids.put(type.id, type);
        }
    }

    private int id;

    ActionType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ActionType fromOperation(Operation operation) {
        return operations.getOrDefault(operation, UNKNOWN);
    }

    public static ActionType fromId(int id) {
        return ids.getOrDefault(id, UNKNOWN);
    }

    public String getHumanReadableVerb() {
        return switch (this.id) {
            case 0 -> "broke";
            case 1 -> "placed";
            case 2 -> "decayed";
            case 3 -> "grew";
            case 4 -> "liquid decayed";
            case 5 -> "liquid spread";
            case 6 -> "modified";
            case 7 -> "used";
            default -> "did unknown action to";
        };
    }
}
