package net.slimediamond.espial.api.query;

public enum Sort {
  /** Chronological order - oldest first */
  CHRONOLOGICAL,

  /** Reverse chronological order - newest first */
  REVERSE_CHRONOLOGICAL,

  /** Newest index last */
  ID_ASCENDING,

  /** Newest index first */
  ID_DESCENDING,

  /** Whatever the database gives us */
  DEFAULT
}
