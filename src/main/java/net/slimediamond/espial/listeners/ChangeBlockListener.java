package net.slimediamond.espial.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.action.BlockAction;
import net.slimediamond.espial.action.ActionType;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.nbt.NBTApplier;
import net.slimediamond.espial.nbt.json.JsonNBTData;
import net.slimediamond.espial.nbt.json.JsonSignData;
import net.slimediamond.espial.transaction.EspialTransactionType;
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChangeBlockListener {

    @Listener(order = Order.EARLY)
    public void preBlockAction(ChangeBlockEvent.Pre event) {
        if (event.cause().root() instanceof Player player) {
            if (Espial.getInstance().getBlockLogService().getInspectingPlayers().contains(player.profile().uuid())) {

                event.setCancelled(true);

                event.locations().forEach(location -> {
                    BlockSnapshot block = location.block().snapshotFor(location);
                    Espial.getInstance().getBlockLogService().processSingle(block.location().get(), player, EspialTransactionType.LOOKUP, null, null, null, true);
                });
            }
        }
    }

    @Listener(order = Order.LATE)
    @IsCancelled(Tristate.FALSE)
    public void onBlockAction(ChangeBlockEvent.All event) {
        @Nullable Living living;
        Object source = event.cause().root();

        if (event.cause().root() instanceof InteractBlockEvent.Primary) {
            source = ((InteractBlockEvent.Primary) event.cause().root()).source();
        } else if (event.cause().root() instanceof InteractBlockEvent.Secondary) {
            source = ((InteractBlockEvent.Secondary) event.cause().root()).source();
        }

        if (source instanceof Player player) {
            if (Espial.getInstance().getBlockLogService().getInspectingPlayers().contains(player.profile().uuid())) {

                event.setCancelled(true);

                BlockSnapshot block = event.transactions().stream().findAny().get().defaultReplacement();

                Espial.getInstance().getBlockLogService().processSingle(block.location().get(), player, EspialTransactionType.LOOKUP, null, null, null, true);
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
                        ActionType.fromOperation(transaction.operation()),
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

                        nbtData.setSignData(new JsonSignData(frontText, backText));
                    });

                    NBTApplier.applyData(nbtData, blockSnapshot.state(), action);
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}