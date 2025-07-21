package net.slimediamond.espial.sponge.transaction;

import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;

import java.util.Collections;
import java.util.List;

public class EmptyTransaction implements Transaction {

    private static final TransactionType TYPE = new EmptyTransactionType();

    @Override
    public TransactionType getTransactionType() {
        return TYPE;
    }

    @Override
    public List<EspialRecord> getRecords() {
        return Collections.emptyList();
    }

    @Override
    public boolean undo() {
        // do nothing
        return true;
    }

}
