package net.slimediamond.espial.api.record;

import net.slimediamond.espial.api.action.Action;

import java.sql.Timestamp;

/**
 * A block record.
 *
 * @author SlimeDiamond
 */
public abstract class BlockRecord extends AbstractRecord {
    public BlockRecord(int id, Timestamp timestamp, boolean rolledBack, Action action) {
        super(id, timestamp, rolledBack, action);
    }
}
