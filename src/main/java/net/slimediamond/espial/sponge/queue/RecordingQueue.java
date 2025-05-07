package net.slimediamond.espial.sponge.queue;

import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.exceptions.RecordSaveException;
import net.slimediamond.espial.api.record.EspialRecord;

import java.util.concurrent.CompletableFuture;

/**
 * The queue on which {@link Action}s are saved to the database
 *
 * <p>This being synchronized, but off the MC server thread is important!</p>
 */
public interface RecordingQueue {

    /**
     * Queue an action to be recorded and put into the database
     *
     * @param action The action to insert
     */
    CompletableFuture<EspialRecord> queue(Action action);

    /**
     * Saves all the pending actions in the queue
     */
    void save() throws RecordSaveException;

}
