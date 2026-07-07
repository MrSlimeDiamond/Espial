package net.slimediamond.espial.sponge.storage;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.storage.EspialStorageException;

import java.util.List;
import java.util.Set;

/**
 * Handles storage for a specific type of record
 *
 * @param <T> The type of record
 */
public interface RecordStorage<T extends EspialRecord> {

    /**
     * Query the backing storage for a collection of records
     *
     * @param query The query to return results for
     * @return A {@link List} of records for the given query of type {@link T}
     */
    List<T> query(EspialQuery query) throws EspialStorageException;

    /**
     * Submit a record to the backing storage
     *
     * @return The ID of the record
     */
    int submit(T record) throws EspialStorageException;

    /**
     * Get the {@link Class} which this record storage handles
     *
     * @return The class type
     */
    Class<T> getType();

    /**
     * Get the events which this record storage is handling
     *
     * @return Handled events
     */
    Set<EspialEvent> getHandledEvents();

}
