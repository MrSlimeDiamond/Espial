package net.slimediamond.espial.api;

import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.submittable.SubmittableResult;
import net.slimediamond.espial.api.transaction.TransactionManager;
import org.spongepowered.api.entity.living.player.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Block log service
 *
 * @author SlimeDiamond
 */
public interface EspialService {
    /**
     * Get the transaction manager
     *
     * @return Transaction manager
     */
    TransactionManager getTransactionManager();

    /**
     * Query a block asynchronously
     *
     * @param query Query
     * @return List of {@link EspialRecord}s relevant to the query
     */
    CompletableFuture<List<EspialRecord>> query(Query query) throws Exception;

    /**
     * Query an ID
     *
     * @param id The ID to query
     * @return Record, if applicable
     */
    Optional<EspialRecord> queryId(int id) throws Exception;

    /**
     * Get an Espial record by its internal ID
     *
     * @param id ID to get from
     * @return {@link Optional} of an {@link EspialRecord} instance
     */
    Optional<EspialRecord> getRecordById(int id) throws Exception;

    /**
     * Submit a query
     *
     * @param query The query to be submitted
     */
    SubmittableResult<List<EspialRecord>> submitQuery(Query query) throws Exception;

    /**
     * Get the player owner of a block
     *
     * @param x X coordinate of the block
     * @param y Y coordinate of the block
     * @param z Z coordinate of the block
     * @return {@link Optional} of a Sponge {@link User} (player) who is the block owner if we can
     * locate one.
     * @throws SQLException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    Optional<User> getBlockOwner(String world, int x, int y, int z)
            throws SQLException, ExecutionException, InterruptedException;

    /**
     * Submit an action to be inserted into the database
     *
     * @param action Action to submit
     */
    SubmittableResult<? extends EspialRecord> submitAction(Action action) throws Exception;
}
