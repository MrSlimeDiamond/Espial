package net.slimediamond.espial.sponge.transaction;

import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.transaction.EspialTransaction;
import net.slimediamond.espial.api.transaction.TransactionManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
    public CompletableFuture<Integer> undo(Object key) {
        Deque<EspialTransaction> undo = undos.getOrDefault(key, new ArrayDeque<>());
        Deque<EspialTransaction> redo = redos.computeIfAbsent(key, k -> new ArrayDeque<>());

        if (!undo.isEmpty()) {
            EspialTransaction transaction = undo.pop();
            redo.push(transaction);

            return process(transaction, true);
        }

        return CompletableFuture.completedFuture(0);
    }

    @Override
    public CompletableFuture<Integer> redo(Object key) {
        Deque<EspialTransaction> redo = redos.getOrDefault(key, new ArrayDeque<>());
        Deque<EspialTransaction> undo = undos.computeIfAbsent(key, k -> new ArrayDeque<>());

        if (!redo.isEmpty()) {
            EspialTransaction transaction = redo.pop();
            undo.push(transaction);

            return process(transaction, false);
        }

        return CompletableFuture.completedFuture(0);
    }

    private CompletableFuture<Integer> process(EspialTransaction transaction, boolean undo) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        Sponge.server().scheduler().submit(Task.builder().execute(() -> {
            try {
                int modifications;
                if (undo) {
                    modifications = transaction.undo();
                } else {
                    modifications = transaction.redo();
                }
                future.complete(modifications);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).plugin(Espial.getInstance().getContainer()).build());
        return future;
    }
}
