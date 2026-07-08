package net.slimediamond.espial.api.storage;


import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialRecord;

import java.util.List;

/**
 * A type of storage
 */
public interface EspialStorage {

    /**
     * Open the storage backend, and initialize everything
     */
    void open() throws EspialStorageException;

    /**
     * Query the storage backend
     *
     * @param query The query
     * @return A list of {@link EspialRecord}s
     */
    List<EspialRecord> query(EspialQuery query);

    /**
     * Submit a record to the backend for long-term storage
     *
     * @param record The record to store
     * @param <T>    The type of record being submitted
     * @return The ID of the record
     */
    <T extends EspialRecord> int submit(T record) throws EspialStorageException;

    /**
     * Set the "rolled back" status on a record
     *
     * @param record The record to set rolled back status on
     * @param rolledBack Whether the record has been rolledb ack
     */
    void setRolledBack(EspialRecord record, boolean rolledBack) throws EspialStorageException;

    /**
     * Set various records to a rolled back state
     *
     * @param records The records on which to set the rolled back state
     * @param rolledBack Whether the records are rolled back
     */
    void batchSetRolledBack(List<EspialRecord> records, boolean rolledBack) throws EspialStorageException;

    /**
     * Batch delete a bunch of records
     *
     * @param ids The IDs to delete en masse
     * @throws EspialStorageException If the database fails to delete the records
     */
    void batchDelete(List<Integer> ids) throws EspialStorageException;
}
