package net.slimediamond.espial.sponge.transaction;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;

import java.util.List;

public class EmptyTransactionType implements TransactionType {

    @Override
    public Transaction apply(final List<EspialRecord> records, final Audience audience) {
        return Transaction.empty();
    }

}
