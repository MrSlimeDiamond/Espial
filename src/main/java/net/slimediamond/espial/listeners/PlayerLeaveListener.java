package net.slimediamond.espial.listeners;

import net.slimediamond.espial.Espial;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.scheduler.ScheduledTask;

public class PlayerLeaveListener {
    @Listener
    public void playerLeave(ServerSideConnectionEvent.Leave event) {
        if (Espial.getInstance().getBlockLogService().getBlockOutlines().containsKey(event.player())) {
            Espial.getInstance().getBlockLogService().getBlockOutlines().get(event.player()).cancel();
            Espial.getInstance().getBlockLogService().getBlockOutlines().remove(event.player());
        }
    }
}
