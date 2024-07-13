package net.slimediamond.espial.listeners;

import net.slimediamond.espial.ActionType;
import net.slimediamond.espial.Database;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;

import java.sql.SQLException;

public class InteractListener {
    private Database database;

    public InteractListener(Database database) {
        this.database = database;
    }

    @Listener
    public void onInteract(InteractBlockEvent.Secondary event) throws SQLException {
        if (event.cause().root() instanceof Player) {
            Player player = (Player) event.cause().root();

            if (event.block().state().type().hasBlockEntity()) {
                database.insertAction(
                        ActionType.INTERACT,
                        player,
                        event.block().world().formatted(),
                        null,
                        event.block());
            }
        }
    }
}
