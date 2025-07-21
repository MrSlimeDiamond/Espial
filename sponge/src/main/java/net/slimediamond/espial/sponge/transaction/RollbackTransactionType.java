package net.slimediamond.espial.sponge.transaction;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import net.slimediamond.espial.sponge.Espial;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class RollbackTransactionType implements TransactionType {

    @Override
    public Transaction apply(final List<EspialRecord> records, final Audience audience) {
        if (records.size() == 0) {
            audience.sendMessage(Format.error("Nothing was rolled back"));
            return Transaction.empty();
        }

        records.sort(Comparator.comparingInt(EspialRecord::getId).reversed());
        TransactionExecutor.run(records, EspialRecord::rollback);

        Sponge.asyncScheduler().submit(Task.builder()
                .execute(() -> {
                    try {
                        Espial.getInstance().getDatabase().batchSetRolledBack(records, true);
                    } catch (SQLException e) {
                        Espial.getInstance().getLogger().error("Unable to batch set rollback on records", e);
                    }
                })
                .plugin(Espial.getInstance().getContainer())
                .build());

        audience.sendMessage(Format.text(String.format("%o record(s) rolled back", records.size())));

        return Transaction.builder()
                .type(TransactionTypes.ROLLBACK.get())
                .records(records)
                .build();
    }

}
