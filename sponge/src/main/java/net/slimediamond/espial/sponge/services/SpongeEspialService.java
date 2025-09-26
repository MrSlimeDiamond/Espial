package net.slimediamond.espial.sponge.services;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.slimediamond.espial.api.preview.PreviewManager;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.services.EspialService;
import net.slimediamond.espial.api.transaction.TransactionManager;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.preview.EspialPreviewManager;
import net.slimediamond.espial.sponge.record.*;
import net.slimediamond.espial.sponge.transaction.EspialTransactionManager;
import net.slimediamond.espial.sponge.utils.formatting.Format;
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
    private final PreviewManager previewManager = new EspialPreviewManager();

    @Override
    public void submit(@NotNull final EspialRecord record) {
        if (Espial.getInstance().getRecordingQueue() == null
                || !Espial.getInstance().getRecordingQueue().isRunning()) {
            // don't bloat the recording queue if we don't want to record
            return;
        }
        if (!(record instanceof final SpongeEspialRecord spongeRecord)) {
            throw new IllegalArgumentException("Non-Sponge EspialRecord submitted to SpongeEspialService");
        }
        if (record instanceof SpongeBlockRecord
                || record instanceof SpongeHangingDeathRecord
                || record instanceof SpongeSignModifyRecord
                || record instanceof SpongeContainerChangeRecord) {
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
                        final String stackTrace = t.toString();
                        query.getAudience().ifPresent(audience ->
                                audience.sendMessage(Format.error("Unable to query for records")
                                        .hoverEvent(HoverEvent.showText(Component.text(stackTrace)))));
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

    @Override
    public PreviewManager getPreviewManager() {
        return previewManager;
    }

}
