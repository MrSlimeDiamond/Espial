package net.slimediamond.espial.listeners;

import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.sponge.user.EspialActorImpl;
import net.slimediamond.espial.util.BlockUtil;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;

import java.util.HashSet;

public class InteractListener {

    @Listener(order = Order.LATE)
    public void onInteract(InteractBlockEvent.Secondary event) throws Exception {
        if (!Espial.getInstance().getConfig().get().logInteractions()) return;

        if (event.cause().root() instanceof Player player) {
            BlockType blockType = event.block().state().type();
            HashSet<BlockType> blocksToCheck = BlockUtil.builder().add(BlockUtil.CONTAINERS).add(BlockUtil.INTERACTIVE).build();

            if (blocksToCheck.contains(blockType)) {
                BlockAction.builder()
                        .type(EventTypes.MODIFY)
                        .blockId(BlockTypes.registry().valueKey(blockType).formatted())
                        .world(event.block().world().formatted())
                        .location(event.block().location().get())
                        .actor(new EspialActorImpl(player))
                        .build().submit();
            }
        }
    }
}
