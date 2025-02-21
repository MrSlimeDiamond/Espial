package net.slimediamond.espial.api;

import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.transaction.TransactionStatus;

import java.util.List;

/**
 * Block log service
 *
 * @author Findlay Richardson (SlimeDiamond)
 */
public interface EspialService {
    /**
     * Query a block
     * @param query Query
     */
    List<BlockAction> query(Query query) throws Exception;

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
     * Submit a query
     * @param query The query to be submitted
     */
    void submit(Query query) throws Exception;

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
