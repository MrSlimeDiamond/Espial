package net.slimediamond.espial.sponge.storage;

import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.registry.EspialRegistryTypes;
import net.slimediamond.espial.api.storage.EspialStorage;
import net.slimediamond.espial.api.storage.EspialStorageException;
import org.spongepowered.api.ResourceKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A storage backend built on {@link RecordStorage} handlers.
 */
public abstract class DefaultStorage implements EspialStorage {

    private final Map<ResourceKey, RecordStorage<?>> submitters;
    private final List<RecordStorage<?>> queriers;

    protected DefaultStorage() {
        this.submitters = new HashMap<>();
        this.queriers = new ArrayList<>();
    }

    protected <T extends EspialRecord> void addSubmitter(final ResourceKey eventLocation, final RecordStorage<T> handler) {
        this.submitters.put(eventLocation, handler);
    }

    protected <T extends EspialRecord> void addQuerier(final RecordStorage<T> handler) {
        this.queriers.add(handler);
    }

    @Override
    public List<EspialRecord> query(final EspialQuery query) {
        // CopyOnWrite list for thread-safety
        final List<EspialRecord> results = new CopyOnWriteArrayList<>();

        queriers.forEach(handler -> {
            // use a bunch of threads to query them simultaneously, so it's fast
//              Sponge.asyncScheduler().submit(Task.builder()
//                      .plugin(Espial.getInstance().getContainer())
//                      .execute(() -> results.addAll(handler.query(query)))
//                      .build(), "Espial query, handler: " + handler.getClass())

            // actually, that might spawn a bunch of threads and we don't know the system config...so just do it on
            // only one thread instead
            try {
                results.addAll(handler.query(query));
            } catch (EspialStorageException e) {
                // TODO: Pass each exception up and then inform the user
                throw new RuntimeException(e);
            }
        });

        return results;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends EspialRecord> int submit(final T record) throws EspialStorageException {
        final ResourceKey eventKey = record.getEvent().key(EspialRegistryTypes.EVENT);
        if (!this.submitters.containsKey(eventKey)) {
            throw new EspialStorageException("Record type '" + record.getClass().getName() + "' not supported", record);
        }
        final RecordStorage<T> handler = (RecordStorage<T>) this.submitters.get(eventKey);
        if (handler == null) {
            throw new EspialStorageException("Record type '" + record.getClass().getName() + "' not supported", record);
        }

        return handler.submit(handler.getType().cast(record));
    }

}
