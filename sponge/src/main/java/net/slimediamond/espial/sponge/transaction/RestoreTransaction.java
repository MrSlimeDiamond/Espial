package net.slimediamond.espial.sponge.transaction;

import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.api.transaction.TransactionTypes;

import java.util.List;

public class RestoreTransaction implements Transaction {

    private final List<EspialRecord> records;

    public RestoreTransaction(final List<EspialRecord> records) {
        this.records = records;
    }

    @Override
    public TransactionType getTransactionType() {
        return TransactionTypes.RESTORE.get();
    }

    @Override
    public List<EspialRecord> getRecords() {
        return records;
    }

    @Override
    public boolean undo() {
        TransactionTypes.ROLLBACK.get().apply(records);
        return true;
    }

}
