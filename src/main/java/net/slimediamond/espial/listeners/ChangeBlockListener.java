package net.slimediamond.espial.listeners;

import net.slimediamond.espial.ActionType;
import net.slimediamond.espial.Database;
import org.checkerframework.checker.nullness.qual.Nullable;
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
        @Nullable Player player;
        Object source = event.cause().root();

        if (event.cause().root() instanceof InteractBlockEvent.Primary) {
            source = ((InteractBlockEvent.Primary) event.cause().root()).source();
        } else if (event.cause().root() instanceof InteractBlockEvent.Secondary) {
            source = ((InteractBlockEvent.Secondary) event.cause().root()).source();
        }

        if (source instanceof Player) {
           player = (Player) source;
        } else {
           player = null; // Server action
        }

        event.transactions().forEach(transaction -> {
            try {
                database.insertAction(
                        ActionType.fromOperation(transaction.operation()),
                        player,
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
