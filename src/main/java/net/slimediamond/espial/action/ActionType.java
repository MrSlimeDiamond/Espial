package net.slimediamond.espial.action;

import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.block.transaction.Operations;

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

    private int id;

    ActionType(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static ActionType fromOperation(Operation operation) {
        if (operation.equals(Operations.BREAK.get())) {
            return ActionType.BREAK;
        } else if (operation.equals(Operations.PLACE.get())) {
            return ActionType.PLACE;
        } else if (operation.equals(Operations.DECAY.get())) {
            return ActionType.DECAY;
        } else if (operation.equals(Operations.GROWTH.get())) {
            return ActionType.GROWTH;
        } else if (operation.equals(Operations.LIQUID_DECAY.get())) {
            return ActionType.LIQUID_DECAY;
        } else if (operation.equals(Operations.LIQUID_SPREAD.get())) {
            return ActionType.LIQUID_SPREAD;
        } else if (operation.equals(Operations.MODIFY.get())) {
            return ActionType.MODIFY;
        }
        return ActionType.UNKNOWN;
    }

    public static ActionType fromId(int id) {
        for (ActionType actionType : values()) {
            if (actionType.id == id) {
                return actionType;
            }
        }
        return ActionType.UNKNOWN;
    }

    public String getHumanReadableVerb() {
        if (this.id == 0) {
            return "broke";
        } else if (this.id == 1) {
            return "placed";
        } else if (this.id == 2) {
            return "decayed";
        } else if (this.id == 3) {
            return "grew";
        } else if (this.id == 4) {
            return "liquid decayed";
        } else if (this.id == 5) {
            return "liquid spread";
        } else if (this.id == 6) {
            return "modified";
        } else if (this.id == 8) {
            return "used";
        } else {
            return "did unknown action to";
        }
    }
}
