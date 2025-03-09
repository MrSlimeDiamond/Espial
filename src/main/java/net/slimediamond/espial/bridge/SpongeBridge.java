package net.slimediamond.espial.bridge;

import org.spongepowered.api.event.block.ChangeBlockEvent;

public interface SpongeBridge {
    Object getRootCause(ChangeBlockEvent event);
}
