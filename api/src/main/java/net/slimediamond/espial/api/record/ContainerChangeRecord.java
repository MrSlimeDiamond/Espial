package net.slimediamond.espial.api.record;

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
     * Get the item which was modified
     *
     * @return Item
     */
    ItemStackSnapshot getItem();

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
         * Set the item which was modified
         *
         * <p><b>This is required</b></p>
         *
         * @param item The item
         * @return This builder, for chaining
         */
        Builder item(ItemStackSnapshot item);

    }

}
