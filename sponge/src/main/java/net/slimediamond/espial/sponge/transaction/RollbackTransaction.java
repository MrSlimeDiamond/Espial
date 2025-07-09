package net.slimediamond.espial.sponge.transaction;

import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.api.transaction.TransactionTypes;

import java.util.List;

public class RollbackTransaction implements Transaction {

    private final List<EspialRecord> records;

    public RollbackTransaction(final List<EspialRecord> records) {
        this.records = records;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionTypes.ROLLBACK.get();
    }

    @Override
    public List<EspialRecord> getRecords() {
        return records;
    }

    @Override
    public boolean undo() {
        TransactionTypes.RESTORE.get().apply(records);
        return true;
    }

}
