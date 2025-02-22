package net.slimediamond.espial.api.record;

import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.transaction.TransactionStatus;

import java.sql.Timestamp;

public abstract class AbstractRecord implements EspialRecord {
    private int id;
    private Timestamp timestamp;
    private boolean rolledBack;
    private Action action;

    public AbstractRecord(
            int id,
            Timestamp timestamp,
            boolean rolledBack,
            Action action
    ) {
        this.id = id;
        this.timestamp = timestamp;
        this.rolledBack = rolledBack;
        this.action = action;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isRolledBack() {
        return rolledBack;
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public abstract TransactionStatus rollback() throws Exception;

    @Override
    public abstract TransactionStatus restore() throws Exception;
}
