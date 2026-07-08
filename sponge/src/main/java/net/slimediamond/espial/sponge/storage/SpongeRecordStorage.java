package net.slimediamond.espial.sponge.storage;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialRecord;
import org.spongepowered.api.registry.DefaultedRegistryReference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class SpongeRecordStorage<T extends EspialRecord> implements RecordStorage<T> {

    public final Class<T> type;
    public Set<DefaultedRegistryReference<EspialEvent>> handledEvents = new HashSet<>();

    public SpongeRecordStorage(final Class<T> type) {
        this.type = type;
    }

    @SafeVarargs
    protected final void supports(final DefaultedRegistryReference<EspialEvent>... events) {
        this.handledEvents.addAll(Arrays.asList(events));
    }

    @Override
    public Class<T> getType() {
        return this.type;
    }

    @Override
    public Set<EspialEvent> getHandledEvents() {
        return this.handledEvents.stream()
                .map(DefaultedRegistryReference::get)
                .collect(Collectors.toSet());
    }

}
