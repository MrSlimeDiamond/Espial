package net.slimediamond.espial.listeners;

import net.slimediamond.espial.api.action.HangingDeathAction;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.sponge.user.EspialActorImpl;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;


public class AttackEntityListener {
    @Listener(order = Order.POST)
    public void onEntityDestruct(DestructEntityEvent event) {
        if (event.cause().root() instanceof Player player) {
            // Hanging entity death (like an item frame)
            if (event.entity() instanceof Hanging hanging) {
                try {
                    HangingDeathAction.builder()
                            .actor(new EspialActorImpl(player))
                            .entity(hanging.type())
                            .world(event.entity().serverLocation().worldKey().formatted())
                            .location(event.entity().serverLocation())
                            .event(EventTypes.HANGING_DEATH)
                            .build().submit();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
