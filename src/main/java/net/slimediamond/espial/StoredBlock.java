package net.slimediamond.espial;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.sql.Timestamp;
import java.util.Optional;

public interface StoredBlock {
    int uid();
    String uuid();
    Timestamp time();
    ActionType actionType();
    String blockId();
    String world();
    Vector3d playerLocation();
    Vector3d playerRotation();
    String itemInHand();
    int x();
    int y();
    int z();
    boolean rolledBack();
    void setNBT(String data);
    Optional<String> getNBT();

    // Not scuffed at all.
    default BlockSnapshot sponge() {
        return BlockSnapshot.builder().from(ServerLocation.of(ResourceKey.of(world().split(":")[0], world().split(":")[1]), x(), y(), z())).build();
    }
}
