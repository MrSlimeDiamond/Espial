package net.slimediamond.espial.api.query;

public enum Sort {
    /**
     * Chronological order - oldest first
     */
    CHRONOLOGICAL,

    /**
     * Reverse chronological order - newest first
     */
    REVERSE_CHRONOLOGICAL,

    /**
     * Whatever the database gives us
     */
    DEFAULT
}
