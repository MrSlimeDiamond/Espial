package net.slimediamond.espial.api.transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionManager {

    /**
     * Submits a transaction to the transaction manager
     *
     * @param user The user who created the transaction
     * @param transaction The transaction
     */
    void submit(UUID user, Transaction transaction);

    /**
     * Get a list of transactions that a user has created
     *
     * @param user The user
     * @return A list of transactions
     */
    List<Transaction> getTransactions(UUID user);

    /**
     * Undoes a transaction and removes it from the queue
     *
     * @param user The user to undo for
     * @return The undone transaction, if present
     */
    Optional<Transaction> undo(UUID user);

    /**
     * Redoes a transaction and removes it from the queue
     *
     * @param user The user to redo for
     * @return The redone transaction, if present
     */
    Optional<Transaction> redo(UUID user);

}
