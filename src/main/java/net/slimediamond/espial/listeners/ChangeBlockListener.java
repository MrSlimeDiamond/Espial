package net.slimediamond.espial.listeners;

import net.slimediamond.espial.ActionType;
import net.slimediamond.espial.Database;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.EspialTransactionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;

import java.sql.SQLException;

public class ChangeBlockListener {
    private Database database;
    public ChangeBlockListener(Database database) {
        this.database = database;
    }

    @Listener
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

            // NOTE: I believe that this is used when NBT data is changed.
            if (transaction.operation().equals(Operations.MODIFY.get())) return;

            try {
                database.insertAction(
                        ActionType.fromOperation(transaction.operation()),
                        living,
                        transaction.finalReplacement().world().formatted(),
                        transaction,
                        null
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}