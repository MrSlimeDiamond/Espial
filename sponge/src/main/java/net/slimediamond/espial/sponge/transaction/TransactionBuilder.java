package net.slimediamond.espial.sponge.transaction;

import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TransactionBuilder implements Transaction.Builder {

    private TransactionType type;
    private List<EspialRecord> records;

    @Override
    public Transaction.Builder type(@NotNull final TransactionType type) {
        this.type = type;
        return this;
    }

    @Override
    public Transaction.Builder records(@NotNull final List<EspialRecord> records) {
        this.records = records;
        return this;
    }

    @Override
    public @NotNull Transaction build() {
        assert this.type != null;
        assert this.records != null;

        if (type.equals(TransactionTypes.ROLLBACK.get())) {
            return new RollbackTransaction(records);
        } else if (type.equals(TransactionTypes.RESTORE.get())) {
            return new RestoreTransaction(records);
        }
        return new EmptyTransaction();
    }

}
