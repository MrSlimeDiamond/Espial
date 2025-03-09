package net.slimediamond.espial.bridge;

import org.spongepowered.api.event.block.ChangeBlockEvent;

public class SpongeBridgeAPI12 implements SpongeBridge {
    @Override
    public Object getRootCause(ChangeBlockEvent event) {
        return event.cause().root();
    }
}
