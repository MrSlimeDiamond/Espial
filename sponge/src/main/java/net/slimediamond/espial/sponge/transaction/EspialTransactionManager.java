package net.slimediamond.espial.sponge.transaction;

import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.api.transaction.TransactionManager;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EspialTransactionManager implements TransactionManager {

    private final Map<UUID, Deque<Transaction>> undos = new HashMap<>();
    private final Map<UUID, Deque<Transaction>> redos = new HashMap<>();

    @Override
    public void submit(final UUID user, final Transaction transaction) {
        undos.computeIfAbsent(user, k -> new ArrayDeque<>()).push(transaction);
        redos.remove(user);
    }

    @Override
    public List<Transaction> getTransactions(final UUID user) {
        return undos.get(user).stream().toList();
    }

    @Override
    public Optional<Transaction> undo(final UUID user) {
        final Deque<Transaction> undo = undos.getOrDefault(user, new ArrayDeque<>());
        final Deque<Transaction> redo = redos.computeIfAbsent(user, k -> new ArrayDeque<>());

        if (!undo.isEmpty()) {
            final Transaction transaction = undo.pop();
            redo.push(transaction);
            transaction.undo();
            return Optional.of(transaction);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Transaction> redo(final UUID user) {
        final Deque<Transaction> redo = redos.getOrDefault(user, new ArrayDeque<>());
        final Deque<Transaction> undo = undos.computeIfAbsent(user, k -> new ArrayDeque<>());

        if (!redo.isEmpty()) {
            final Transaction transaction = redo.pop();
            undo.push(transaction);
            transaction.apply();
            return Optional.of(transaction);
        }
        return Optional.empty();
    }

}
