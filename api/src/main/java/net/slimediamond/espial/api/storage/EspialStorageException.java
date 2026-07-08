package net.slimediamond.espial.api.storage;

import net.slimediamond.espial.api.record.EspialRecord;

import java.util.Collection;
import java.util.Collections;

public class EspialStorageException extends Exception {

    private final Collection<EspialRecord> records;

    public EspialStorageException(final Throwable cause) {
        super(cause);
        this.records = Collections.emptyList();
    }

    public EspialStorageException(final Throwable cause, final Collection<EspialRecord> records) {
        super(cause);
        this.records = records;
    }

    public EspialStorageException(final Throwable cause, final EspialRecord record) {
        super(cause);
        this.records = Collections.singleton(record);
    }

    public EspialStorageException(final String message, final Collection<EspialRecord> records) {
        super(message);
        this.records = records;
    }

    public EspialStorageException(final String message, final Throwable cause, final Collection<EspialRecord> records) {
        super(message, cause);
        this.records = records;
    }

    public EspialStorageException(final String message, final EspialRecord record) {
        super(message);
        this.records = Collections.singleton(record);
    }

    public EspialStorageException(final String message, final Throwable cause, final EspialRecord record) {
        super(message, cause);
        this.records = Collections.singleton(record);
    }

    /**
     * Get the records affected by the error
     *
     * @return Records affected
     */
    Collection<EspialRecord> getRecords() {
        return this.records;
    }

}
