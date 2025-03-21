package net.slimediamond.espial.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.action.event.EventTypes;
import net.slimediamond.espial.api.nbt.json.JsonNBTData;
import net.slimediamond.espial.api.nbt.json.JsonSignData;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.query.Sort;
import net.slimediamond.espial.api.user.EspialActor;
import net.slimediamond.espial.sponge.user.EspialActorImpl;
import net.slimediamond.espial.util.BlockUtil;
import net.slimediamond.espial.util.NBTApplier;
import net.slimediamond.espial.util.SpongeUtil;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockListeners {
    @Listener(order = Order.EARLY)
    public void preBlockAction(ChangeBlockEvent.Pre event) throws Exception {
        if (event.cause().root() instanceof Player player) {
            if (Espial.getInstance().getInspectingPlayers().contains(player.profile().uuid())) {

                event.setCancelled(true);

                for (ServerLocation location : event.locations()) {
                    Query.builder()
                            .type(QueryType.LOOKUP)
                            .min(location)
                            .sort(Sort.REVERSE_CHRONOLOGICAL)
                            .caller(player)
                            .audience(player)
                            .spread(true)
                            .build()
                            .submit();
                }
            }
        }
    }

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.FALSE)
    public void onBlockChange(ChangeBlockEvent.All event) throws Exception {
        // Don't track events caused from commands
        if (event.cause().containsType(CommandMapping.class)) {
            return;
        }

        Living living = event.cause().first(Living.class).orElse(null);

        // Only track changes from living entities
        // which can modify blocks
        if (living == null) {
            return;
        }

        Optional<Player> player = event.cause().first(Player.class);
        if (player.isPresent()) {
            if (Espial.getInstance().getInspectingPlayers().contains(player.get().profile().uuid())) {

                event.setCancelled(true);

                BlockSnapshot block = event.transactions().stream().findAny().get().defaultReplacement();

                Query.builder()
                        .type(QueryType.LOOKUP)
                        .min(block.location().get())
                        .caller(player)
                        .sort(Sort.REVERSE_CHRONOLOGICAL)
                        .audience(player.get())
                        .spread(true)
                        .build()
                        .submit();
                return;
            }
        }

        event.transactions().forEach(transaction -> {
            try {
                EventType type = EventTypes.fromSponge(transaction.operation());
                BlockSnapshot snapshot;

                // If something other than air, specify what this action
                // is to be rolled back to.
                String rollbackTo = null;

                // someone placed something, so the block will be the *after*
                if (transaction.operation().equals(Operations.PLACE.get())
                        || transaction.operation().equals(Operations.GROWTH.get())) {
                    snapshot = transaction.finalReplacement();

                    BlockType before = transaction.original().state().type();
                    if (!BlockUtil.AIR.contains(before)) {
                        rollbackTo = SpongeUtil.getBlockId(before);
                    }
                } else {
                    // something was broken, so get what it was *before*
                    snapshot = transaction.original();
                }

                // Only log modifications that are actually useful
                if (transaction.operation().equals(Operations.MODIFY.get())
                        && !BlockUtil.MODIFIABLE.contains(snapshot.state().type())) {
                    return;
                }

                JsonNBTData jsonNBTData = new JsonNBTData();

                if (rollbackTo != null) {
                    jsonNBTData.setRollbackBlock(rollbackTo);
                }

                if (BlockUtil.SIGNS.contains(snapshot.state().type())) {
                    snapshot.createArchetype().ifPresent(blockEntity -> {
                        List<Component> frontComponents = null;
                        List<Component> backComponents = null;

                        if (blockEntity.supports(Keys.SIGN_FRONT_TEXT)) {
                            frontComponents = blockEntity
                                    .get(Keys.SIGN_FRONT_TEXT)
                                    .map(text -> new ArrayList<>(text.lines().get()))
                                    .orElseGet(ArrayList::new);
                        }

                        if (blockEntity.supports(Keys.SIGN_BACK_TEXT)) {
                            backComponents = blockEntity
                                    .get(Keys.SIGN_BACK_TEXT)
                                    .map(text -> new ArrayList<>(text.lines().get()))
                                    .orElseGet(ArrayList::new);
                        }

                        List<String> frontText = null;
                        List<String> backText = null;

                        if (frontComponents != null) {
                            frontText = frontComponents.stream()
                                    .map(component ->
                                            GsonComponentSerializer.gson().serialize(component))
                                    .toList();
                        }

                        if (backComponents != null) {
                            backText = backComponents.stream()
                                    .map(component ->
                                            GsonComponentSerializer.gson().serialize(component))
                                    .toList();
                        }

                        if (frontComponents != null && backComponents != null) {
                            jsonNBTData.setSignData(new JsonSignData(frontText, backText));
                        }
                    });
                }

                EspialActor actor = new EspialActorImpl(living);

                BlockAction.Builder builder = BlockAction.builder()
                        .event(type)
                        .actor(actor)
                        .event(EventTypes.fromSponge(transaction.operation()))
                        .snapshot(snapshot);

                // only submit nbt data if it will be useful
                if (NBTApplier.update(jsonNBTData, snapshot.state()) || rollbackTo != null) {
                    builder.withNBTData(jsonNBTData);
                }

                builder.build().submit();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
