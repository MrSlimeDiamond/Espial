package net.slimediamond.espial.api.record;

import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.transaction.TransactionStatus;

import java.sql.Timestamp;

public class EntityRecord extends AbstractRecord {

    public EntityRecord(int id, Timestamp timestamp, boolean rolledBack, Action action) {
        super(id, timestamp, rolledBack, action);
    }

    @Override
    public TransactionStatus rollback() throws Exception {
        return TransactionStatus.UNSUPPORTED;
    }

    @Override
    public TransactionStatus restore() throws Exception {
        return TransactionStatus.UNSUPPORTED;
    }
}
