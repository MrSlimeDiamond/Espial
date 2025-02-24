package net.slimediamond.espial.api.query;

public enum QueryType {
  LOOKUP(false),
  ROLLBACK(true),
  RESTORE(true);

  private final boolean reversible;

  QueryType(boolean reversible) {
    this.reversible = reversible;
  }

  public boolean isReversible() {
    return this.reversible;
  }
}
