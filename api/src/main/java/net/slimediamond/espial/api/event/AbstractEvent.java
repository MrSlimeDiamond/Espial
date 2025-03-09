package net.slimediamond.espial.api.event;

import net.slimediamond.espial.api.user.EspialActor;

/**
 * An event.
 *
 * @author SlimeDiamond
 */
public abstract class AbstractEvent {
  private EspialActor actor;

  public AbstractEvent(EspialActor actor) {
    this.actor = actor;
  }

  public EspialActor getActor() {
    return this.actor;
  }
}
