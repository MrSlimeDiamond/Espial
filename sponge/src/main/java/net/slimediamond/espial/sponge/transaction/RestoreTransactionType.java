package net.slimediamond.espial.sponge.transaction;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.Task;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class RestoreTransactionType implements TransactionType {

    @Override
    public Transaction apply(final List<EspialRecord> records, final Audience audience) {
        if (records.isEmpty()) {
            audience.sendMessage(Format.error("Nothing was restored"));
            return Transaction.empty();
        }

        records.sort(Comparator.comparingInt(EspialRecord::getId));
        TransactionExecutor.run(records, EspialRecord::restore);

        Sponge.asyncScheduler().submit(Task.builder()
                .execute(() -> {
                    try {
                        Espial.getInstance().getDatabase().batchSetRolledBack(records, false);
                    } catch (final SQLException e) {
                        Espial.getInstance().getLogger().error("Unable to batch set restore on records", e);
                    }
                })
                .plugin(Espial.getInstance().getContainer())
                .build());

        audience.sendMessage(Format.text(String.format("%o record(s) restored", records.size())));

        return Transaction.builder()
                .type(TransactionTypes.RESTORE.get())
                .records(records)
                .build();
    }

    @Override
    public Transaction preview(final List<EspialRecord> records, final ServerPlayer viewer) {
        // only block records can be previewed really
        records.sort(Comparator.comparingInt(EspialRecord::getId));
        records.stream()
                .filter(record -> record instanceof BlockRecord)
                .forEach(record -> viewer.sendBlockChange(record.getLocation().blockPosition(),
                        ((BlockRecord) record).getReplacementBlock().state()));

        return Transaction.builder()
                .type(TransactionTypes.RESTORE.get())
                .records(records)
                .build();
    }

}
