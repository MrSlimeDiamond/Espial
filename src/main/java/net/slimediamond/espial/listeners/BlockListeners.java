package net.slimediamond.espial.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.api.nbt.NBTApplier;
import net.slimediamond.espial.api.nbt.json.JsonNBTData;
import net.slimediamond.espial.api.nbt.json.JsonSignData;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.query.Sort;
import net.slimediamond.espial.api.user.EspialActor;
import net.slimediamond.espial.sponge.user.EspialActorImpl;
import net.slimediamond.espial.util.BlockUtil;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.ArrayList;
import java.util.List;

public class BlockListeners {
    @Listener(order = Order.EARLY)
    public void preBlockAction(ChangeBlockEvent.Pre event) throws Exception {
        if (event.cause().root() instanceof Player player) {
            if (Espial.getInstance().getInspectingPlayers()
                    .contains(player.profile().uuid())) {

                event.setCancelled(true);

                for (ServerLocation location : event.locations()) {
                    Query.builder()
                            .type(QueryType.LOOKUP)
                            .min(location)
                            .sort(Sort.REVERSE_CHRONOLOGICAL)
                            .caller(player)
                            .audience(player)
                            .spread(true)
                            .build().submit();
                }
            }
        }
    }

    @Listener(order = Order.LATE)
    @IsCancelled(Tristate.FALSE)
    public void onBlockAction(ChangeBlockEvent.All event) throws Exception {
        @Nullable Living living;
        Object source = event.cause().root();

        if (event.cause().root() instanceof InteractBlockEvent.Primary) {
            source = ((InteractBlockEvent.Primary) event.cause()
                    .root()).source();
        } else if (event.cause()
                .root() instanceof InteractBlockEvent.Secondary) {
            source = ((InteractBlockEvent.Secondary) event.cause()
                    .root()).source();
        }

        if (source instanceof Player player) {
            if (Espial.getInstance().getInspectingPlayers()
                    .contains(player.profile().uuid())) {

                event.setCancelled(true);

                BlockSnapshot block =
                        event.transactions().stream().findAny().get()
                                .defaultReplacement();

                Query.builder()
                        .type(QueryType.LOOKUP)
                        .min(block.location().get())
                        .caller(player)
                        .sort(Sort.REVERSE_CHRONOLOGICAL)
                        .audience(player)
                        .spread(true)
                        .build().submit();
                return;
            }
        }

        if (source instanceof Living) {
            living = (Living) source;
        } else {
            if (!Espial.getInstance().getConfig().get().logServerChanges()) {
                return;
            }
            living = null; // Server action
        }

        event.transactions().forEach(transaction -> {
            // These are almost always useless, and just flood the database.
            // It's stuff like "this water spread"

            if (transaction.operation().equals(Operations.MODIFY.get()) &&
                    living == null) {
                return;
            }

            try {
                EventType type = EventTypes.fromSponge(transaction.operation());
                BlockSnapshot snapshot;

                if (transaction.operation().equals(Operations.PLACE.get())) {
                    snapshot = transaction.finalReplacement();
                } else {
                    snapshot = transaction.original();
                }

                JsonNBTData jsonNBTData = new JsonNBTData();

                if (BlockUtil.SIGNS.contains(snapshot.state().type())) {
                    snapshot.createArchetype().ifPresent(blockEntity -> {
                        List<Component> frontComponents = null;
                        List<Component> backComponents = null;

                        if (blockEntity.supports(Keys.SIGN_FRONT_TEXT)) {
                            frontComponents =
                                    blockEntity.get(Keys.SIGN_FRONT_TEXT)
                                            .map(text -> new ArrayList<>(
                                                    text.lines().get()))
                                            .orElseGet(ArrayList::new);
                        }

                        if (blockEntity.supports(Keys.SIGN_BACK_TEXT)) {
                            backComponents =
                                    blockEntity.get(Keys.SIGN_BACK_TEXT)
                                            .map(text -> new ArrayList<>(
                                                    text.lines().get()))
                                            .orElseGet(ArrayList::new);
                        }

                        List<String> frontText = null;
                        List<String> backText = null;

                        if (frontComponents != null) {
                            frontText = frontComponents.stream()
                                    .map(component -> GsonComponentSerializer.gson()
                                            .serialize(component)).toList();
                        }

                        if (backComponents != null) {
                            backText = backComponents.stream()
                                    .map(component -> GsonComponentSerializer.gson()
                                            .serialize(component)).toList();
                        }

                        if (frontComponents != null && backComponents != null) {
                            jsonNBTData.setSignData(
                                    new JsonSignData(frontText, backText));
                        }
                    });
                }

                EspialActor actor = new EspialActorImpl(living);

                BlockAction.Builder builder = BlockAction.builder()
                        .blockId(snapshot.state().type()
                                .key(RegistryTypes.BLOCK_TYPE).formatted())
                        .event(type)
                        .actor(actor)
                        .event(EventTypes.fromSponge(transaction.operation()))
                        .location(snapshot.location().get())
                        .world(snapshot.location().get().worldKey()
                                .formatted());

                if (NBTApplier.update(jsonNBTData, snapshot.state())) {
                    builder.withNBTData(jsonNBTData);
                }

                builder.build().submit();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}