package net.slimediamond.espial.api.event;

import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.user.EspialActor;

public class EspialPreInsertActionEvent extends AbstractCancellable {
  private Action action;

  public EspialPreInsertActionEvent(EspialActor actor, Action action) {
    super(actor);
    this.action = action;
  }

  public Action getAction() {
    return this.action;
  }
}
