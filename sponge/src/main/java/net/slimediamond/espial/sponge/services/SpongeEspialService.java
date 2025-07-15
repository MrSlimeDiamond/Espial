package net.slimediamond.espial.sponge.services;

import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.services.EspialService;
import net.slimediamond.espial.api.transaction.TransactionManager;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.record.SpongeBlockRecord;
import net.slimediamond.espial.sponge.record.SpongeEspialRecord;
import net.slimediamond.espial.common.utils.formatting.Format;
import net.slimediamond.espial.sponge.record.SpongeHangingDeathRecord;
import net.slimediamond.espial.sponge.transaction.EspialTransactionManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SpongeEspialService implements EspialService {

    private final TransactionManager transactionManager = new EspialTransactionManager();
    private final List<UUID> inspectingUsers = new ArrayList<>();

    @Override
    public void submit(@NotNull final EspialRecord record) {
        if (!(record instanceof final SpongeEspialRecord spongeRecord)) {
            throw new IllegalArgumentException("Non-Sponge EspialRecord submitted to SpongeEspialService");
        }
        if (record instanceof SpongeBlockRecord
                || record instanceof SpongeHangingDeathRecord) {
            Espial.getInstance().getRecordingQueue().getQueue().add(spongeRecord);
            return;
        }
        throw new IllegalArgumentException("Record type " + record.getClass().getName() + " is not supported");
    }

    @Override
    public CompletableFuture<List<EspialRecord>> query(@NotNull final EspialQuery query) {
        final CompletableFuture<List<EspialRecord>> future = new CompletableFuture<>();
        Sponge.asyncScheduler().submit(Task.builder()
                .execute(() -> {
                    try {
                        future.complete(Espial.getInstance().getDatabase().query(query));
                    } catch (final Throwable t) {
                        query.getAudience().ifPresent(audience ->
                                audience.sendMessage(Format.error("Unable to query for records. Check the console")));
                        Espial.getInstance().getLogger().error("Unable to query for records", t);
                        future.completeExceptionally(t);
                    }
                })
                .plugin(Espial.getInstance().getContainer())
                .build());
        return future;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public List<UUID> getInspectingUsers() {
        return inspectingUsers;
    }

}
