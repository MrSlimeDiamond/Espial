package net.slimediamond.espial.util;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.sponge.SpongeAdapter;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

public class PlayerSelectionUtil {
    public static Optional<Pair<ServerLocation, ServerLocation>> getWorldEditRegion(Player player) {
        WorldEdit worldEdit = WorldEdit.getInstance();
        com.sk89q.worldedit.entity.Player wePlayer = SpongeAdapter.adapt((ServerPlayer) player);
        LocalSession localSession = worldEdit.getSessionManager().get(wePlayer);

        try {
            Region region = localSession.getSelection(localSession.getSelectionWorld());
            if (region != null) {
                BlockVector3 regionMin = region.getMinimumPoint();
                BlockVector3 regionMax = region.getMaximumPoint();

                Vector3d regionMin3d = new Vector3d(regionMin.getX(), regionMin.getY(), regionMin.getZ());
                Vector3d regionMax3d = new Vector3d(regionMax.getX(), regionMax.getY(), regionMax.getZ());

                ServerLocation serverLocationMin = ServerLocation.of(player.serverLocation().world(), regionMin3d);
                ServerLocation serverLocationMax = ServerLocation.of(player.serverLocation().world(), regionMax3d);

                return Optional.of(Pair.of(serverLocationMin, serverLocationMax));
            }
        } catch (IncompleteRegionException e) {
            // Handle exception if no region is selected
        }

        return Optional.empty();
    }

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
