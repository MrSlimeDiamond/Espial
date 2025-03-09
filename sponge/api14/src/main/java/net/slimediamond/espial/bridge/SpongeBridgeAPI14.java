package net.slimediamond.espial.bridge;

import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;

public class SpongeBridgeAPI14 implements SpongeBridge{
    @Override
    public Object getRootCause(ChangeBlockEvent event) {
        Object source = event.cause().root();
        if (event.cause().root() instanceof InteractBlockEvent.Primary.Start) {
            source = ((InteractBlockEvent.Primary) event.cause().root()).source();
        } else if (event.cause().root() instanceof InteractBlockEvent.Secondary.Pre) {
            source = ((InteractBlockEvent.Secondary) event.cause().root()).source();
        } else if (event.cause().root() instanceof InteractItemEvent.Secondary) {
            source = ((InteractItemEvent.Secondary) event.cause().root()).source();
        }

        return source;
    }
}
