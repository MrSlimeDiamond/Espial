package net.slimediamond.espial.sponge.transaction;

import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.sponge.Espial;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class TransactionExecutor {

    public static void run (final List<EspialRecord> records, final Consumer<EspialRecord> consumer) {
        run(records, consumer, 500);
    }

    public static void run(final List<EspialRecord> records, final Consumer<EspialRecord> consumer,
                    final int batchSize) {
        final Iterator<EspialRecord> iterator = records.iterator();
        Sponge.server().scheduler().submit(Task.builder()
                .interval(Ticks.of(1))
                .execute(task -> {
                    for (int i = 0; i < batchSize && iterator.hasNext(); i++) {
                        final EspialRecord record = iterator.next();
                        consumer.accept(record);
                    }
                    if (!iterator.hasNext()) {
                        task.cancel();
                    }
                })
                .plugin(Espial.getInstance().getContainer())
                .build());
    }

}
