package net.slimediamond.espial.api.query;

/**
 * Query types.
 *
 * @author SlimeDiamond
 */
public enum QueryType {
  /**
   * A lookup to show the player.
   */
  LOOKUP(false),
  /**
   * A rollback. Takes blocks back in time.
   */
  ROLLBACK(true),
  /**
   * A restore. Takes blocks forwards in time.
   */
  RESTORE(true);

  private final boolean reversible;

  QueryType(boolean reversible) {
    this.reversible = reversible;
  }

  /**
   * Whether this type of query can be undone
   * and if it should be added as a {@link net.slimediamond.espial.api.transaction.EspialTransaction}
   *
   * @return Whether this query type can be undone
   */
  public boolean isReversible() {
    return this.reversible;
  }
}
