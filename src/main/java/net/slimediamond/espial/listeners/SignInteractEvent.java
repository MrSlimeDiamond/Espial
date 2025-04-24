package net.slimediamond.espial.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.api.nbt.json.JsonNBTData;
import net.slimediamond.espial.api.nbt.json.JsonSignData;
import net.slimediamond.espial.api.user.EspialActor;
import net.slimediamond.espial.sponge.user.EspialActorImpl;
import net.slimediamond.espial.util.SpongeUtil;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;

import java.util.List;

public class SignInteractEvent {
    @Listener(order = Order.LATE)
    public void onSignChangeEvent(ChangeSignEvent event) throws Exception {
        if (event.cause().root() instanceof Living source) {

            List<Component> newText = event.text().get();
            List<Component> currentFront = event.sign().frontText().lines().get();
            List<Component> currentBack = event.sign().backText().lines().get();

            List<String> frontSerialized;
            List<String> backSerialized;

            if (event.isFrontSide()) {
                frontSerialized = newText.stream()
                        .map(component -> GsonComponentSerializer.gson().serialize(component))
                        .toList();
                backSerialized = currentBack.stream()
                        .map(component -> GsonComponentSerializer.gson().serialize(component))
                        .toList();
            } else {
                frontSerialized = currentFront.stream()
                        .map(component -> GsonComponentSerializer.gson().serialize(component))
                        .toList();
                backSerialized = newText.stream()
                        .map(component -> GsonComponentSerializer.gson().serialize(component))
                        .toList();
            }

            JsonNBTData nbtData = new JsonNBTData();
            nbtData.setSignData(new JsonSignData(frontSerialized, backSerialized));

            EspialActor actor = new EspialActorImpl(source);

            BlockAction.builder()
                    .event(EventTypes.MODIFY)
                    .world(event.sign().serverLocation().worldKey().formatted())
                    .blockId(SpongeUtil.getBlockId(event.sign().block().type()))
                    .actor(actor)
                    .location(event.sign().serverLocation())
                    .withNBTData(nbtData)
                    .build()
                    .submit();
        }
    }
}
