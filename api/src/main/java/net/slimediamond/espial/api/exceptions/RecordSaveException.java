package net.slimediamond.espial.api.exceptions;

public class RecordSaveException extends Exception {
    public RecordSaveException(String message) {
        super(message);
    }

    public RecordSaveException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecordSaveException(Throwable cause) {
        super(cause);
    }

    protected RecordSaveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
