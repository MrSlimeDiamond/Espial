package net.slimediamond.espial.listeners;

import net.slimediamond.espial.ActionType;
import net.slimediamond.espial.Database;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.util.BlockUtil;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;

import java.sql.SQLException;
import java.util.HashSet;

public class InteractListener {
    private Database database;

    public InteractListener(Database database) {
        this.database = database;
    }

    @Listener
    public void onInteract(InteractBlockEvent.Secondary event) throws SQLException {
        if (!Espial.getInstance().getConfig().get().logInteractions()) return;

        if (event.cause().root() instanceof Player) {
            Player player = (Player) event.cause().root();

            BlockType blockType = event.block().state().type();
            HashSet<BlockType> blocksToCheck = BlockUtil.builder().add(BlockUtil.CONTAINERS).add(BlockUtil.INTERACTIVE).build();

            if (blocksToCheck.contains(blockType)) {
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
