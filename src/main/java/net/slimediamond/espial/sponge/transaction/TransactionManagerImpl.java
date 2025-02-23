package net.slimediamond.espial.sponge.transaction;

import net.slimediamond.espial.api.transaction.EspialTransaction;
import net.slimediamond.espial.api.transaction.TransactionManager;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class TransactionManagerImpl implements TransactionManager {
    private final Map<Object, Deque<EspialTransaction>> undos = new HashMap<>();
    private final Map<Object, Deque<EspialTransaction>> redos = new HashMap<>();

    @Override
    public void add(Object key, EspialTransaction transaction) {
        undos.computeIfAbsent(key, k -> new ArrayDeque<>()).push(transaction);
        redos.remove(key);
    }

    @Override
    public void remove(Object key, EspialTransaction transaction) {
        undos.getOrDefault(key, new ArrayDeque<>()).remove(transaction);
        redos.getOrDefault(key, new ArrayDeque<>()).remove(transaction);
    }

    @Override
    public int undo(Object key) throws Exception {
        Deque<EspialTransaction> undo =
                undos.getOrDefault(key, new ArrayDeque<>());
        Deque<EspialTransaction> redo =
                redos.computeIfAbsent(key, k -> new ArrayDeque<>());

        if (!undo.isEmpty()) {
            EspialTransaction transaction = undo.pop();
            redo.push(transaction);

            return transaction.undo();
        }

        return 0;
    }

    @Override
    public int redo(Object key) throws Exception {
        Deque<EspialTransaction> redo =
                redos.getOrDefault(key, new ArrayDeque<>());
        Deque<EspialTransaction> undo =
                undos.computeIfAbsent(key, k -> new ArrayDeque<>());

        if (!redo.isEmpty()) {
            EspialTransaction transaction = redo.pop();
            undo.push(transaction);

            return transaction.redo();
        }

        return 0;
    }
}
