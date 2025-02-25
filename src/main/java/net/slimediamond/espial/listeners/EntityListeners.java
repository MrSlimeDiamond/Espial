package net.slimediamond.espial.listeners;

import net.slimediamond.espial.api.action.HangingDeathAction;
import net.slimediamond.espial.api.action.ItemFrameRemoveAction;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.api.nbt.NBTData;
import net.slimediamond.espial.api.nbt.json.JsonNBTData;
import net.slimediamond.espial.sponge.user.EspialActorImpl;
import net.slimediamond.espial.sponge.user.ServerActor;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.item.ItemTypes;

public class EntityListeners {
  @Listener(order = Order.POST)
  public void onEntityDestruct(DestructEntityEvent event) {
    if (event.entity() instanceof Hanging hanging) {
      try {
        NBTData nbtData = new JsonNBTData(hanging.hangingDirection().get(), null, null, false);

        HangingDeathAction.Builder builder =
            HangingDeathAction.builder()
                .entity(hanging.type())
                .world(event.entity().serverLocation().worldKey().formatted())
                .location(event.entity().serverLocation())
                .event(EventTypes.HANGING_DEATH)
                .withNBTData(nbtData);

        if (event.cause().root() instanceof Player player) {
          builder.actor(new EspialActorImpl(player));
        } else {
          // Likely caused by the block an item frame is on
          // was broken or something similar.

          // TODO: We should track the contents of the item frame here
          builder.actor(new ServerActor());
        }

        builder.build().submit();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Listener(order = Order.POST)
  public void onAttackEntity(AttackEntityEvent event) throws Exception {
    // log item frame contents when it's removed
    if (event.entity() instanceof ItemFrame itemFrame) {
      if (event.cause().root() instanceof DamageSource damageSource) {
        if (damageSource.indirectSource().isPresent()) {
          if (damageSource.indirectSource().get() instanceof Player player) {
            if (!itemFrame.item().get().type().equals(ItemTypes.AIR.get())) {
              ItemFrameRemoveAction.builder()
                  .itemType(itemFrame.item().get().type())
                  .actor(new EspialActorImpl(player))
                  .location(itemFrame.serverLocation())
                  .world(event.entity().serverLocation().worldKey().formatted())
                  .event(EventTypes.ITEM_FRAME_REMOVE)
                  .build()
                  .submit();
            }
          }
        }
      }
    }
  }
}
