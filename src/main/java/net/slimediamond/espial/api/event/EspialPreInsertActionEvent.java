package net.slimediamond.espial.api.event;

import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.user.EspialActor;

/**
 * An event that is fired before an action is inserted into
 * the database and becomes a record.
 *
 * @author SlimeDiamond
 */
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
