package net.slimediamond.espial.api.event;

import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.user.EspialActor;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Cause;

public class PreInsertActionEvent extends EspialEvent implements Cancellable {
    private final Action action;
    private boolean cancelled;

    public PreInsertActionEvent(EspialActor actor, Cause cause, Action action) {
        super(actor, cause);
        this.action = action;

        this.cancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public Action getAction() {
        return action;
    }
}
