package net.slimediamond.espial.api.query;

public enum QueryType {
    LOOKUP(false),
    ROLLBACK(true),
    RESTORE(true);

    private boolean reversible;

    QueryType(boolean reversible) {
        this.reversible = reversible;
    }

    public boolean isReversible() {
        return this.reversible;
    }
}
