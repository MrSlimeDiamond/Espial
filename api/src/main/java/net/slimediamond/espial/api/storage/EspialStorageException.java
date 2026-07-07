package net.slimediamond.espial.api.storage;

import net.slimediamond.espial.api.record.EspialRecord;

import java.util.List;

public class EspialStorageException extends Exception {

    private final List<EspialRecord> records;

    public EspialStorageException(final String message, final List<EspialRecord> records) {
        super(message);
        this.records = records;
    }

    public EspialStorageException(final String message, final Throwable cause, final List<EspialRecord> records) {
        super(message, cause);
        this.records = records;
    }

    public EspialStorageException(final String message, final EspialRecord record) {
        super(message);
        this.records = List.of(record);
    }

    public EspialStorageException(final String message, final Throwable cause, final EspialRecord record) {
        super(message, cause);
        this.records = List.of(record);
    }

    /**
     * Get the records affected by the error
     *
     * @return Records affected
     */
    List<EspialRecord> getRecords() {
        return this.records;
    }

}
