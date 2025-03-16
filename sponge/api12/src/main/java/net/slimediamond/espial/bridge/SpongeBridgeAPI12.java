package net.slimediamond.espial.bridge;

import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;

public class SpongeBridgeAPI12 implements SpongeBridge {
  @Override
  public Object getRootCause(ChangeBlockEvent event) {
    Object source = event.cause().root();
    if (source instanceof InteractBlockEvent.Primary primary) {
      return primary.source();
    } else if (source instanceof InteractBlockEvent.Secondary secondary) {
      return secondary.source();
    } else if (source instanceof InteractItemEvent.Secondary itemSecondary) {
      return itemSecondary.source();
    } else if (source instanceof ChangeBlockEvent.All all) {
      source = getRootCause(all);
    }

    return source;
  }
}
