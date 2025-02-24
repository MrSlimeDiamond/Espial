package net.slimediamond.espial.util;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.LocatableBlock;

import java.util.Optional;

public class RayTraceUtil {
  public static Optional<LocatableBlock> getBlockFacingPlayer(Player player) {
    Optional<RayTraceResult<LocatableBlock>> result =
        RayTrace.block()
            .sourceEyePosition(player)
            .direction(player)
            .world(player.serverLocation().world())
            .limit(4)
            .execute();

    return result.map(RayTraceResult::selectedObject);
  }
}
