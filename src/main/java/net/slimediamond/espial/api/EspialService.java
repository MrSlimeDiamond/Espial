package net.slimediamond.espial.api;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.transaction.EspialTransaction;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;

import java.util.List;

/**
 * Block log service
 *
 * @author Findlay Richardson (SlimeDiamond)
 */
public interface EspialService {
    /**
     * Set the sign data for a specific action.
     * @param action Action to set sign data for
     */
    void setSignData(BlockAction action);

    /**
     * Query a block
     * @param query Query
     */
    List<BlockAction> query(Query query) throws Exception;

    /**
     * Generate a {@link List} of components from some {@link BlockAction}s
     * @param actions Actions to lookup
     * @param spread Whether to spread the results
     * @return Contents
     */
    List<Component> generateLookupContents(List<BlockAction> actions, boolean spread);

    /**
     * Execute a command which uses queries
     * @param context Command context
     * @param type Type to query
     * @return Command result
     */
    CommandResult execute(CommandContext context, QueryType type);

    /**
     * Undo an action
     * @param action Action to undo
     * @return Status
     */
    TransactionStatus rollback(BlockAction action) throws Exception;

    /**
     * Restore/redo an action
     * @param action Action to restore
     * @return Status
     */
    TransactionStatus restore(BlockAction action) throws Exception;

    /**
     * Process a query from a player
     * @param query The query to process
     * @param audience Where to send the output to
     * @param spread Whether to spread the results
     */
    void process(Query query, Audience audience, boolean spread) throws Exception;

    /**
     * Submit a transaction
     * @param transaction Transaction to submit
     */
    void submit(EspialTransaction transaction) throws Exception;

    default void rollbackAll(List<BlockAction> actions) throws Exception {
        for (BlockAction action : actions) {
            this.rollback(action);
        }
    }

    default void restoreAll(List<BlockAction> actions) throws Exception {
        for (BlockAction action : actions) {
            this.restore(action);
        }
    }
}
