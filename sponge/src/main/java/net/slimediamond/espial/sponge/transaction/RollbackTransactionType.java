package net.slimediamond.espial.sponge.transaction;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionType;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.utils.VolumeUtils;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RollbackTransactionType implements TransactionType {

    @Override
    public Transaction apply(final List<EspialRecord> records, final Audience audience) {
        if (records.isEmpty()) {
            audience.sendMessage(Format.error("Nothing was rolled back"));
            return Transaction.empty();
        }

        records.sort(Comparator.comparingInt(EspialRecord::getId).reversed());

        Sponge.server().scheduler().submit(Task.builder()
                .execute(() -> {
                    // handle block records specially. This way is more efficient
                    records.stream()
                            .filter(record -> record instanceof BlockRecord)
                            .map(BlockRecord.class::cast)
                            .collect(Collectors.groupingBy(r -> r.getLocation().world()))
                            .forEach((world, rs) -> VolumeUtils.applyBlockRecords(world, rs, true));

                    // and then regular for everything that isn't a block record
                    records.stream()
                            .filter(record -> !(record instanceof BlockRecord))
                            .forEach(EspialRecord::rollback);
                })
                .plugin(Espial.getInstance().getContainer())
                .build(), "rollback");

        Sponge.asyncScheduler().submit(Task.builder()
                .execute(() -> {
                    try {
                        Espial.getInstance().getDatabase().batchSetRolledBack(records, true);
                    } catch (final SQLException e) {
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
