package net.slimediamond.espial.listeners;

import net.slimediamond.espial.Espial;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public class PlayerLeaveListener {
    @Listener
    public void playerLeave(ServerSideConnectionEvent.Leave event) {
        if (Espial.getInstance().getBlockOutlines().containsKey(event.player())) {
            Espial.getInstance().getBlockOutlines().get(event.player()).cancel();
            Espial.getInstance().getBlockOutlines().remove(event.player());
        }
    }
}
