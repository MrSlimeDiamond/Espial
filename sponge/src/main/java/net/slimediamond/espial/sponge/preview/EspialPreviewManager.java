package net.slimediamond.espial.sponge.preview;

import net.slimediamond.espial.api.preview.PreviewManager;
import net.slimediamond.espial.api.transaction.Transaction;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.math.vector.Vector3i;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class EspialPreviewManager implements PreviewManager {

    private final Map<UUID, Transaction> previews = new HashMap<>();

    @Override
    public Map<UUID, Transaction> getPreviews() {
        return previews;
    }

    @Override
    public boolean apply(final UUID player) {
        if (!previews.containsKey(player)) {
            return false;
        }
        previews.get(player).apply();
        previews.remove(player);
        return true;
    }

    @Override
    public boolean cancel(final ServerPlayer player) {
        if (!previews.containsKey(player.uniqueId())) {
            return false;
        }

        final Transaction transaction = previews.get(player.uniqueId());
        final Set<Vector3i> positions = transaction.getRecords().stream()
                .map(r -> r.getLocation().position().toInt())
                .collect(Collectors.toSet());

        positions.forEach(player::resetBlockChange);

        previews.remove(player.uniqueId());

        return true;
    }

    @Override
    public void submit(final UUID uuid, final Transaction transaction) {
        previews.put(uuid, transaction);
    }

}
