package net.slimediamond.espial.sponge.event;

import net.slimediamond.espial.api.event.InsertRecordEvent;
import net.slimediamond.espial.api.record.EspialRecord;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Cause;

public class SpongeInsertRecordEvent implements InsertRecordEvent {

    private final EspialRecord record;
    private final Cause cause;

    public SpongeInsertRecordEvent(final EspialRecord record, final Cause cause) {
        this.record = record;
        this.cause = cause;
    }

    @Override
    public EspialRecord getRecord() {
        return record;
    }

    @Override
    public Cause cause() {
        return cause;
    }

    public static final class PreImpl extends CancellableImpl implements InsertRecordEvent.Pre {

        private final EspialRecord record;
        private final Cause cause;

        public PreImpl(final EspialRecord record, final Cause cause) {
            this.record = record;
            this.cause = cause;
        }

        @Override
        public EspialRecord getRecord() {
            return record;
        }

        @Override
        public Cause cause() {
            return cause;
        }

    }

    public static final class PostImpl implements InsertRecordEvent.Post {

        private final EspialRecord record;
        private final Cause cause;

        public PostImpl(final EspialRecord record, final Cause cause) {
            this.record = record;
            this.cause = cause;
        }

        @Override
        public EspialRecord getRecord() {
            return record;
        }

        @Override
        public Cause cause() {
            return cause;
        }

    }

    private static class CancellableImpl implements Cancellable {

        private boolean cancelled = false;

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(final boolean cancel) {
            this.cancelled = cancel;
        }

    }

}
