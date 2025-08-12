package net.slimediamond.espial.sponge.utils;

import net.slimediamond.espial.api.record.BlockRecord;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeCollectors;
import org.spongepowered.math.vector.Vector3i;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class VolumeUtils {

    public static void applyBlockRecords(final ServerWorld world,
                                    final List<BlockRecord> records,
                                    final boolean rollback) {
        final Set<Vector3i> positions = records.stream()
                .map(r -> r.getLocation().position().toInt())
                .collect(Collectors.toSet());

        final Vector3i min = positions.stream()
                .reduce(Vector3i::min)
                .orElseThrow();

        final Vector3i max = positions.stream()
                .reduce(Vector3i::max)
                .orElseThrow();

        final Map<Vector3i, BlockRecord> snapshots = records.stream()
                .collect(Collectors.toMap(
                        r -> r.getLocation().position().toInt(),
                        r -> r,
                        rollback
                                ? (a, b) -> a.getId() < b.getId() ? a : b  // earliest for rollback
                                : (a, b) -> a.getId() > b.getId() ? a : b  // latest for restore
                ));

        final List<BlockSnapshot> blockEntities = new LinkedList<>();

        world.blockStateStream(min, max, StreamOptions.lazily())
                .filter(e -> snapshots.containsKey(e.position().toInt()))
                .map(e -> {
                    final BlockSnapshot snapshot = getSnapshot(snapshots.get(e.position().toInt()), rollback);
                    if (snapshot.state().type().hasBlockEntity()) {
                        // we'll need to handle it specially later
                        blockEntities.add(snapshot);
                    }
                    return snapshot.state();
                })
                .apply(VolumeCollectors.applyBlocksToWorld(world));

        // now for special things!
        blockEntities.forEach(snapshot -> snapshot.restore(true, BlockChangeFlags.DEFAULT_PLACEMENT));
    }

    private static BlockSnapshot getSnapshot(final BlockRecord record, final boolean rollback) {
        if (rollback) {
            return record.getOriginalBlock();
        } else {
            return record.getReplacementBlock();
        }
    }

}
