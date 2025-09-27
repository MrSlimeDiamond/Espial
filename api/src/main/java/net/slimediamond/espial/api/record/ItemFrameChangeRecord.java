package net.slimediamond.espial.api.record;

import net.slimediamond.espial.api.event.EspialEvents;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public interface ItemFrameChangeRecord extends EspialRecord {

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
