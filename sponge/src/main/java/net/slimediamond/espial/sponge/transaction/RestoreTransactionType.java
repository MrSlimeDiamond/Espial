package net.slimediamond.espial.sponge.transaction;

import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.sponge.Espial;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.sql.SQLException;
import java.util.List;

public class RestoreTransactionType implements TransactionType {

    @Override
    public Transaction apply(final List<EspialRecord> records) {
        TransactionExecutor.run(records, EspialRecord::restore);

        Sponge.asyncScheduler().submit(Task.builder()
                .execute(() -> {
                    try {
                        Espial.getInstance().getDatabase().batchSetRolledBack(records, false);
                    } catch (SQLException e) {
                        Espial.getInstance().getLogger().error("Unable to batch set restore on records", e);
                    }
                })
                .plugin(Espial.getInstance().getContainer())
                .build());

        return Transaction.builder()
                .type(TransactionTypes.RESTORE.get())
                .records(records)
                .build();
    }

}
