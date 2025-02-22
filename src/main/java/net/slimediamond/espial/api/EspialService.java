package net.slimediamond.espial.api;

import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.submittable.SubmittableResult;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Block log service
 *
 * @author Findlay Richardson (SlimeDiamond)
 */
public interface EspialService {
    /**
     * Query a block
     * @param query Query
     */
    List<EspialRecord> query(Query query) throws Exception;

    /**
     * Submit a query
     * @param query The query to be submitted
     */
    SubmittableResult<List<EspialRecord>> submitQuery(Query query) throws Exception;

    /**
     * Roll back a block action
     * @param record Record to roll back
     * @return Status of the rollback
     * @throws Exception If something goes wrong
     */
    TransactionStatus rollbackBlock(BlockRecord record) throws Exception;

    /**
     * Restore a block action which has been rolled back
     * @param record Record to restore
     * @return Status of the restore
     * @throws Exception If something goes wrong
     */
    TransactionStatus restoreBlock(BlockRecord record) throws Exception;

    /**
     * Get the player owner of a block
     * @param x X coordinate of the block
     * @param y Y coordinate of the block
     * @param z Z coordinate of the block
     * @return {@link Optional} of a Sponge {@link User} (player) who is the block owner if we can locate one.
     * @throws SQLException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    Optional<User> getBlockOwner(int x, int y, int z) throws SQLException, ExecutionException, InterruptedException;

    /**
     * Submit an action to be inserted into the database
     * @param action Action to submit
     */
    SubmittableResult<? extends EspialRecord> submitAction(Action action) throws Exception;
}
