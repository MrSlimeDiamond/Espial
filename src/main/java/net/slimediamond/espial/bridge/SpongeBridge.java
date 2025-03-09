package net.slimediamond.espial.bridge;

import org.spongepowered.api.event.block.ChangeBlockEvent;

/**
 * For handling cases when things are handled differently on other SpongeAPI versions
 *
 * @author SlimeDiamond
 */
public interface SpongeBridge {
    /**
     * Get the root cause of a {@link ChangeBlockEvent}
     *
     * @param event Event to get the root cause of
     * @return      Event's root cause
     */
    Object getRootCause(ChangeBlockEvent event);
}
