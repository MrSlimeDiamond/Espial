package net.slimediamond.espial.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.type.ActionTypes;
import net.slimediamond.espial.api.nbt.NBTApplier;
import net.slimediamond.espial.api.nbt.json.JsonNBTData;
import net.slimediamond.espial.api.nbt.json.JsonSignData;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.query.Sort;
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
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChangeBlockListener {

    @Listener(order = Order.EARLY)
    public void preBlockAction(ChangeBlockEvent.Pre event) throws Exception {
        if (event.cause().root() instanceof Player player) {
            if (Espial.getInstance().getInspectingPlayers().contains(player.profile().uuid())) {

                event.setCancelled(true);

                for (ServerLocation location : event.locations()) {
                    Query query = Query.builder()
                            .setType(QueryType.LOOKUP)
                            .setMin(location)
                            .setSort(Sort.REVERSE_CHRONOLOGICAL)
                            .setUser(player)
                            .setAudience(player)
                            .setSpread(true)
                            .build();
                    Espial.getInstance().getEspialService().submit(query);
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
            source = ((InteractBlockEvent.Primary) event.cause().root()).source();
        } else if (event.cause().root() instanceof InteractBlockEvent.Secondary) {
            source = ((InteractBlockEvent.Secondary) event.cause().root()).source();
        }

        if (source instanceof Player player) {
            if (Espial.getInstance().getInspectingPlayers().contains(player.profile().uuid())) {

                event.setCancelled(true);

                BlockSnapshot block = event.transactions().stream().findAny().get().defaultReplacement();

                Query query = Query.builder()
                        .setType(QueryType.LOOKUP)
                        .setMin(block.location().get())
                        .setUser(player)
                        .setSort(Sort.REVERSE_CHRONOLOGICAL)
                        .setAudience(player)
                        .setSpread(true)
                        .build();
                Espial.getInstance().getEspialService().submit(query);
                return;
            }
        }

        if (source instanceof Living) {
            living = (Living) source;
        } else {
            if (!Espial.getInstance().getConfig().get().logServerChanges()) return;
            living = null; // Server action
        }

        event.transactions().forEach(transaction -> {
            // These are almost always useless, and just flood the database.
            // It's stuff like "this water spread"

            if (transaction.operation().equals(Operations.MODIFY.get()) && living == null) return;

            try {
                Optional<BlockAction> actionOptional = Espial.getInstance().getDatabase().insertAction(
                        ActionTypes.fromSponge(transaction.operation()),
                        living,
                        transaction.finalReplacement().world().formatted(),
                        transaction,
                        null
                );

                actionOptional.ifPresent(action -> {
                    BlockSnapshot blockSnapshot;
                    if (transaction.operation().equals(Operations.PLACE.get())) {
                        blockSnapshot = transaction.defaultReplacement();
                    } else {
                        blockSnapshot = transaction.original();
                    }

                    JsonNBTData nbtData = new JsonNBTData();

                    if (BlockUtil.SIGNS.contains(blockSnapshot.state().type())) {
                        blockSnapshot.createArchetype().ifPresent(blockEntity -> {
                            List<Component> frontComponents = null;
                            List<Component> backComponents = null;

                            if (blockEntity.supports(Keys.SIGN_FRONT_TEXT)) {
                                frontComponents = blockEntity.get(Keys.SIGN_FRONT_TEXT)
                                        .map(text -> new ArrayList<>(text.lines().get()))
                                        .orElseGet(ArrayList::new);
                            }

                            if (blockEntity.supports(Keys.SIGN_BACK_TEXT)) {
                                backComponents = blockEntity.get(Keys.SIGN_BACK_TEXT)
                                        .map(text -> new ArrayList<>(text.lines().get()))
                                        .orElseGet(ArrayList::new);
                            }

                            List<String> frontText = null;
                            List<String> backText = null;

                            if (frontComponents != null) {
                                frontText = frontComponents.stream().map(component -> GsonComponentSerializer.gson().serialize(component)).toList();
                            }

                            if (backComponents != null) {
                                backText = backComponents.stream().map(component -> GsonComponentSerializer.gson().serialize(component)).toList();
                            }

                            if (frontComponents != null && backComponents != null) {
                                nbtData.setSignData(new JsonSignData(frontText, backText));
                            }
                        });
                    }

                    NBTApplier.applyData(nbtData, blockSnapshot.state(), action);
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}