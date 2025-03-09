package net.slimediamond.espial.api.event;

import net.slimediamond.espial.api.user.EspialActor;

/**
 * An event which can be cancelled
 *
 * @author SlimeDiamond
 */
public abstract class AbstractCancellable extends AbstractEvent {
  private boolean cancelled;

  public AbstractCancellable(EspialActor actor) {
    super(actor);
  }

  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  public boolean isCancelled() {
    return this.cancelled;
  }
}
