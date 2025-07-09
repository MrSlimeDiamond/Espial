package net.slimediamond.espial.sponge.transaction;

import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.sponge.Espial;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.List;

public class RestoreTransactionType implements TransactionType {

    @Override
    public boolean canBeUndone() {
        return true;
    }

    @Override
    public Transaction apply(final List<EspialRecord> records) {
        Sponge.server().scheduler().submit(Task.builder()
                .execute(() -> records.forEach(EspialRecord::restore))
                .plugin(Espial.getInstance().getContainer())
                .build());

        return Transaction.builder()
                .type(TransactionTypes.RESTORE.get())
                .records(records)
                .build();
    }

}
