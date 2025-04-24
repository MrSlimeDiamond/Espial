package net.slimediamond.espial.api.event;

import net.slimediamond.espial.api.user.EspialActor;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public abstract class EspialEvent extends AbstractEvent {
    private final EspialActor actor;
    private final Cause cause;

    public EspialEvent(EspialActor actor, Cause cause) {
        this.actor = actor;
        this.cause = cause;
    }

    @Override
    public Cause cause() {
        return cause;
    }

    public EspialActor getActor() {
        return actor;
    }
}
