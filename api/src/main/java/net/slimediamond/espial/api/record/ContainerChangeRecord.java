package net.slimediamond.espial.api.record;

import net.slimediamond.espial.api.event.EspialEvents;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

/**
 * A record triggered by changing the contents of a container
 */
public interface ContainerChangeRecord extends EspialRecord {

    /**
     * Get the slot affected by the event
     *
     * @return Slot within the container
     */
    int getSlot();

    /**
     * Get the original item in the container
     *
     * @return Original item
     */
    ItemStackSnapshot getOriginal();

    /**
     * Get the replacement item in the container
     *
     * @return Replacement item
     */
    ItemStackSnapshot getReplacement();

    /**
     * Get the item affected by the event. In the case of item additions,
     * this is the replacement block. Otherwise, it is the original
     * item.
     *
     * @return Affected item
     */
    default ItemStackSnapshot getAffectedItem() {
        return this.getEvent().equals(EspialEvents.ITEM_INSERT.get())
                ? this.getReplacement()
                : this.getOriginal();
    }

    static Builder builder() {
        return Sponge.game().builderProvider().provide(Builder.class);
    }

    interface Builder extends EspialRecord.Builder<Builder> {

        /**
         * Set the slot affected by the event
         *
         * <p><b>This is required</b></p>
         *
         * @param slot The slot affected
         * @return This builder, for chaining
         */
        Builder slot(int slot);

        /**
         * Set the original item in the container
         *
         * <p><b>This is required</b></p>
         *
         * @param original The original item
         * @return This builder, for chaining
         */
        Builder original(ItemStackSnapshot original);

        /**
         * Set the replacement item in the container
         *
         * <p><b>This is required</b></p>
         *
         * @param replacement The replacement item
         * @return This builder, for chaining
         */
        Builder replacement(ItemStackSnapshot replacement);

    }

}
