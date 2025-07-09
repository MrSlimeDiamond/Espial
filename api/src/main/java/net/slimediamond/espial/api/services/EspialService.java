package net.slimediamond.espial.api.services;

import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.TransactionManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface EspialService {

    /**
     * Submits an {@link EspialRecord}.
     *
     * @param record The record to submit
     */
    void submit(@NotNull EspialRecord record);

    /**
     * Query for records within particular parameters
     *
     * @param query The query
     * @return The found records
     */
    CompletableFuture<List<EspialRecord>> query(@NotNull EspialQuery query);

    /**
     * Get the transaction manager
     *
     * @return Transaction manager
     */
    TransactionManager getTransactionManager();

    /**
     * Get a list of users who are currently using the interactive mode
     *
     * <p>This list can be added to</p>
     *
     * @return Inspecting players
     */
    List<UUID> getInspectingUsers();

}
