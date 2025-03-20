package net.slimediamond.espial.sponge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.EspialService;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.event.EventManager;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.submittable.SubmittableResult;
import net.slimediamond.espial.api.transaction.EspialTransaction;
import net.slimediamond.espial.api.transaction.EspialTransactionImpl;
import net.slimediamond.espial.api.transaction.TransactionManager;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import net.slimediamond.espial.sponge.event.EventManagerImpl;
import net.slimediamond.espial.sponge.transaction.TransactionManagerImpl;
import net.slimediamond.espial.util.Format;
import net.slimediamond.espial.util.SpongeUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class EspialServiceImpl implements EspialService {
    private final TransactionManager transactionManager;
    private final EventManager eventManager;

    public EspialServiceImpl() {
        this.transactionManager = new TransactionManagerImpl();
        this.eventManager = new EventManagerImpl();
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public EventManager getEventManager() {
        return eventManager;
    }

    @Override
    public CompletableFuture<List<EspialRecord>> query(Query query) {
        CompletableFuture<List<EspialRecord>> future = new CompletableFuture<>();

        Sponge.asyncScheduler().submit(Task.builder()
                .execute(() -> future.complete(queryStorage(query)))
                .plugin(Espial.getInstance().getContainer())
                .build(), "Espial query");

        return future;
    }

    // Error handling logic for the above method
    private List<EspialRecord> queryStorage(Query query) {
        try {
            return Espial.getInstance().getDatabase().query(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<EspialRecord> queryId(int id) throws Exception {
        return Optional.ofNullable(Espial.getInstance().getDatabase().queryId(id));
    }

    @Override
    public Optional<EspialRecord> getRecordById(int id) throws Exception {
        return Optional.ofNullable(Espial.getInstance().getDatabase().queryId(id));
    }

    @Override
    public SubmittableResult<List<EspialRecord>> submitQuery(Query query) {
        AtomicReference<List<EspialRecord>> results = new AtomicReference<>();
        this.query(query).thenAccept(records -> {
            results.set(records);

            StringBuilder argsPreview = new StringBuilder();

            try {
                if (query.getPlayerUUIDs() != null && !query.getPlayerUUIDs().isEmpty()) {
                    List<String> players = new ArrayList<>();
                    for (UUID uuid : query.getPlayerUUIDs()) {
                        players.add(Sponge.server().userManager().load(uuid).get().get().name());
                    }
                    argsPreview.append(" Players: ").append("[").append(String.join(", ", players)).append("]");
                }
            } catch (InterruptedException | ExecutionException e) {
                // Give some indication
                argsPreview.append(" Player: (error processing)");
                e.printStackTrace();
            }

            if (query.getBlockIds() != null && !query.getBlockIds().isEmpty()) {
                List<String> blocks = new ArrayList<>();
                for (String block : query.getBlockIds()) {
                    blocks.add(block.split(":")[1]);
                }
                argsPreview.append(" Blocks: ").append("[").append(String.join(", ", blocks)).append("]");
            }

            if (query.getTimestamp() != null && !query.getTimestamp().equals(Timestamp.from(Instant.EPOCH))) {
                argsPreview.append(" After: ").append(Format.date(query.getTimestamp()));
            }

            if (query.getType() == QueryType.ROLLBACK || query.getType() == QueryType.RESTORE) {
                final String msg;
                if (query.getType().equals(QueryType.ROLLBACK)) {
                    msg = "rolled back";
                } else {
                    msg = "restored";
                }

                Sponge.asyncScheduler().submit(Task.builder().execute(() -> {
                    List<Integer> success = new ArrayList<>();
                    AtomicInteger skipped = new AtomicInteger();

                    List<CompletableFuture<EspialRecord>> futures = records.stream().map(record -> {
                        CompletableFuture<EspialRecord> future = new CompletableFuture<>();
                        Sponge.server().scheduler().submit(Task.builder().execute(() -> {
                            try {
                                TransactionStatus status;
                                switch (query.getType()) {
                                    case ROLLBACK -> status = record.rollback();
                                    case RESTORE -> status = record.restore();
                                    default -> status = TransactionStatus.UNSUPPORTED;
                                }

                                if (status == TransactionStatus.SUCCESS) {
                                    success.add(record.getId());
                                } else {
                                    skipped.getAndIncrement();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                query.getAudience().sendMessage(Format.error("Exception when processing ID: " + record.getId()));
                            } finally {
                                future.complete(record);
                            }
                        }).plugin(Espial.getInstance().getContainer()).build(), "Espial processing: " + record.getId());
                        return future;
                    }).toList();

                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                    TextComponent.Builder builder = Component.text();

                    if (!success.isEmpty()) {
                        builder.append(Component.text(success.size()))
                                .append(Component.text(" action(s) were "))
                                .append(Component.text(msg)).color(NamedTextColor.WHITE);

                        // Commit a transaction but only if we will use it later
                        // for example: lookups won't be added here, but rollbacks will
                        if (query.getType().isReversible()) {
                            EspialTransaction transaction = new EspialTransactionImpl(success, query);
                            Espial.getInstance().getTransactionManager().add(transaction.getUser(), transaction);
                        }

                    } else {
                        builder.append(Component.text("Nothing was " + msg).color(NamedTextColor.WHITE));
                    }

                    if (skipped.get() != 0) {
                        builder.append(Component.text(", with " + skipped + " skipped").color(NamedTextColor.WHITE));
                    }

                    builder.append(Component.text(".").color(NamedTextColor.WHITE));

                    query.getAudience().sendMessage(Format.component(builder.build()));
                }).plugin(Espial.getInstance().getContainer()).build());
            } else if (query.getType() == QueryType.LOOKUP) {
                List<Component> contents = Format.generateLookupContents(records, query.isSpread());

                if (contents.isEmpty()) {
                    query.getAudience().sendMessage(Format.error("No data was found."));

                    return;
                }

                PaginationList.Builder builder = PaginationList.builder()
                        .title(Format.title("Lookup results"))
                        .padding(Format.PADDING);

                if (!argsPreview.isEmpty()) {
                    builder.header(Format.truncate(Component.text("Parameters:")
                            .color(Format.HINT_COLOR)
                            .append(Component.text(argsPreview.toString()).color(NamedTextColor.GRAY))));
                }
                builder.contents(contents).sendTo(query.getAudience());

            } else {
                // Some other query type that we don't currently support
                query.getAudience().sendMessage(Format.error("This query type is not supported."));
            }
        });

        return SubmittableResult.of(results.get());
    }

    @Override
    public SubmittableResult<? extends EspialRecord> submitAction(Action action) throws Exception {
        Optional<EspialRecord> result = Espial.getInstance().getDatabase().submit(action);
        return result
                .<SubmittableResult<? extends EspialRecord>>map(SubmittableResult::new)
                .orElse(null);
    }

    @Override
    public Optional<User> getBlockOwner(String world, int x, int y, int z)
            throws SQLException, ExecutionException, InterruptedException {
        Optional<User> userOptional = Espial.getInstance().getDatabase().getBlockOwner(world, x, y, z);

        if (userOptional.isEmpty()) {
            ServerWorld serverWorld = SpongeUtil.getWorld(world).orElseThrow(() ->
                    new RuntimeException("getBlockOwner supplied a non-existent world."));
            return ServerLocation.of(serverWorld, x, y, z).createSnapshot().creator().flatMap(uuid -> {
                try {
                    return Sponge.server().userManager().load(uuid).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            return userOptional;
        }
    }

    private void process(List<EspialRecord> records, Query query, String argsPreview) throws Exception {

    }
}
