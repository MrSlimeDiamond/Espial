package net.slimediamond.espial.api.record;

import net.slimediamond.espial.api.action.Action;

import java.sql.Timestamp;

/**
 * An entity record.
 *
 * @author SlimeDiamond
 */
public abstract class EntityRecord extends AbstractRecord {
    private final Action action;

    public EntityRecord(int id, Timestamp timestamp, boolean rolledBack, Action action) {
        super(id, timestamp, rolledBack, action);
        this.action = action;
    }
}
