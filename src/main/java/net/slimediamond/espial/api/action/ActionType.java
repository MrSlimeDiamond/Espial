package net.slimediamond.espial.api.action;

/**
 * The possible action types
 */
public enum ActionType {
    /**
     * A block was modified
     */
    BLOCK,
    /**
     * A hanging entity (like an item frame) was killed
     */
    HANGING_DEATH,
    /**
     * Item frame item removal
     */
    ITEM_FRAME_REMOVE
}
