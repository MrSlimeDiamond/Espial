package net.slimediamond.espial.api.record;

import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.transaction.TransactionStatus;

import java.sql.Timestamp;


public class BlockRecord extends AbstractRecord {
    public BlockRecord(int id, Timestamp timestamp, boolean rolledBack, Action action) {
        super(id, timestamp, rolledBack, action);
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
