package net.slimediamond.espial.api;

import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.submittable.SubmittableResult;
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
    List<EspialRecord> query(Query query) throws Exception;

    /**
     * Submit a query
     * @param query The query to be submitted
     */
    void submitQuery(Query query) throws Exception;

    TransactionStatus rollbackBlock(BlockRecord record) throws Exception;
    TransactionStatus restoreBlock(BlockRecord record) throws Exception;

    /**
     * Submit an action to be inserted into the database
     * @param action Action to submit
     */
    SubmittableResult<? extends EspialRecord> submitAction(Action action) throws Exception;
}
