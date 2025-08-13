package net.slimediamond.espial.api.preview;

import net.slimediamond.espial.api.transaction.Transaction;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Map;
import java.util.UUID;

public interface PreviewManager {

    /**
     * Get a map of previews which each player has
     *
     * <p>The value is a {@link Transaction}, it may be applied
     * with {@link Transaction#apply()}</p>
     *
     * @return Previews
     */
    Map<UUID, Transaction> getPreviews();

    /**
     * Apply the preview a player currently has
     *
     * @param player The player
     * @return {@code true} if successful, {@code false} if
     * the player had no transaction they were previewing
     */
    boolean apply(UUID player);

    /**
     * Cancel the transaction which a player was previewing
     *
     * @param player The player
     * @return {@code true} if successful, {@code false} if
     * the player had no transaction they were previewing
     */
    boolean cancel(ServerPlayer player);

    /**
     * Submit a preview to the preview manager, from which
     * changes can be made
     *
     * @param uuid The UUID of the player
     * @param transaction The transaction which the preview uses
     */
    void submit(UUID uuid, Transaction transaction);

}
