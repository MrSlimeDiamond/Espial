package net.slimediamond.espial.sponge.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.exceptions.RecordSaveException;
import net.slimediamond.espial.api.record.EspialRecord;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class RecordingQueueImpl implements RecordingQueue {
    private final Queue<Action> queue = new LinkedBlockingQueue<>();
    private final Map<Action, CompletableFuture<EspialRecord>> futures = new HashMap<>();

    @Override
    public CompletableFuture<EspialRecord> queue(Action action) {
        queue.add(action);
        CompletableFuture<EspialRecord> future = new CompletableFuture<>();
        futures.put(action, future);
        return future;
    }

    @Override
    public void save() throws RecordSaveException {
        for (Action action : queue) {
            try {
                EspialRecord record = Espial.getInstance().getDatabase().submit(action)
                        .orElseThrow(() -> new RecordSaveException("No record was present"));
                futures.get(action).complete(record);
                queue.remove(action);
            } catch (SQLException | JsonProcessingException e) {
                throw new RecordSaveException(e);
            }
        }
    }
}
