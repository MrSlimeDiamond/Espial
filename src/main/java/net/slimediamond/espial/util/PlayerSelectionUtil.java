package net.slimediamond.espial.util;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

public class PlayerSelectionUtil {
    public static Pair<ServerLocation, ServerLocation> getCuboidAroundPlayer(Player player, int range) {
        Vector3d pos = player.position();

        double minX = pos.x() - range;
        double minY = pos.y() - range;
        double minZ = pos.z() - range;

        double maxX = pos.x() + range;
        double maxY = pos.y() + range;
        double maxZ = pos.z() + range;

        return Pair.of(
                ServerLocation.of(player.serverLocation().world(), new Vector3d(minX, minY, minZ)),
                ServerLocation.of(player.serverLocation().world(), new Vector3d(maxX, maxY, maxZ))
        );
    }
}
