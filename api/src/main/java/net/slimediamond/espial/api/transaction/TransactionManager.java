package net.slimediamond.espial.api.transaction;

import java.util.concurrent.CompletableFuture;

/**
 * Manages {@link EspialTransaction}s.
 *
 * @author SlimeDiamond
 */
public interface TransactionManager {
    /**
     * Add a transaction to a user
     *
     * @param key         The identifier (user)
     * @param transaction The transaction to add
     */
    void add(Object key, EspialTransaction transaction);

    /**
     * Remove a transaction from a user
     *
     * @param key         The identifier (user)
     * @param transaction The transaction to remove
     */
    void remove(Object key, EspialTransaction transaction);

    /**
     * Undo latest transaction of a user
     *
     * @param key The identifier (user)
     * @return Amount of actions undone
     */
    CompletableFuture<Integer> undo(Object key);

    /**
     * Redo the latest undone transaction of a user
     *
     * @param key The identifier (user)
     * @return Amount of actions redone
     */
    CompletableFuture<Integer> redo(Object key);
}
