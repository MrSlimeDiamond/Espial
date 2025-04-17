package net.slimediamond.espial.api.record;

import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.transaction.TransactionStatus;

import java.sql.Timestamp;

/**
 * A record from the database.
 *
 * @author SlimeDiamond
 */
public interface EspialRecord {
    /**
     * The ID (primary key value) of the record.
     *
     * @return Internal ID
     */
    int getId();

    /**
     * Get the time this action happened
     *
     * @return Timestamp
     */
    Timestamp getTimestamp();

    /**
     * Whether the block has been rolled back
     *
     * @return Rollback status
     */
    boolean isRolledBack();

    /**
     * Get the associated action
     *
     * @return Action
     */
    Action getAction();

    /**
     * Roll back this action
     *
     * @return Status of the rollback
     */
    TransactionStatus rollback() throws Exception;

    /**
     * Restore this action
     *
     * @return Status of the restore
     */
    TransactionStatus restore() throws Exception;

    /**
     * Rollback this action
     *
     * @param force Whether to roll back regardless of whether it's already been rolled back
     * @return Status of the rollback
     */
    TransactionStatus rollback(boolean force) throws Exception;

    /**
     * Rollback this action
     *
     * @param force Whether to restore regardless of whether it's already been restored
     * @return Status of the restore
     */
    TransactionStatus restore(boolean force) throws Exception;

}
