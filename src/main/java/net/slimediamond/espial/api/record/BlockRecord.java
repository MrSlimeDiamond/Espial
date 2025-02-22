package net.slimediamond.espial.api.record;

import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.transaction.TransactionStatus;

import java.sql.Timestamp;

public class BlockRecord implements EspialRecord {
    private int id;
    private Timestamp timestamp;
    private boolean rolledBack;
    private Action action;

    public BlockRecord(
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
    public TransactionStatus rollback() throws Exception {
        return Espial.getInstance().getEspialService().rollbackBlock(this);
    }

    @Override
    public TransactionStatus restore() throws Exception {
        return Espial.getInstance().getEspialService().restoreBlock(this);
    }
}
