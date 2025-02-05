package net.slimediamond.espial.listeners;

import net.slimediamond.espial.ActionType;
import net.slimediamond.espial.Database;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.sql.SQLException;

public class ChangeBlockListener {
    private Database database;
    public ChangeBlockListener(Database database) {
        this.database = database;
    }

    @Listener
    public void onBlockAction(ChangeBlockEvent.All event) {
        @Nullable Player player;

        if (event.cause().root() instanceof Player) {
           player = (Player) event.cause().root();
           //playerId = player.profile().uuid().toString();
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
