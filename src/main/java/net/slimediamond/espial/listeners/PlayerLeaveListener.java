package net.slimediamond.espial.listeners;

import net.slimediamond.espial.Espial;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.scheduler.ScheduledTask;

public class PlayerLeaveListener {
    @Listener
    public void playerLeave(ServerSideConnectionEvent.Leave event) {
        if (Espial.blockOutlines.containsKey(event.player())) {
            Espial.blockOutlines.get(event.player()).cancel();
            Espial.blockOutlines.remove(event.player());
        }
    }
}
