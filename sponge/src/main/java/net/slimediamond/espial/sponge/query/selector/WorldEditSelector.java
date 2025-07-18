package net.slimediamond.espial.sponge.query.selector;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.sponge.SpongeAdapter;
import net.kyori.adventure.text.Component;
import net.slimediamond.espial.common.utils.formatting.Format;
import net.slimediamond.espial.sponge.commands.subsystem.Parameters;
import net.slimediamond.espial.sponge.utils.CommandUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

public class WorldEditSelector implements Selector {

    @Override
    public Vector3iRange select(@NotNull final CommandContext context) throws CommandException {
        if (Sponge.pluginManager().plugin("worldedit").isEmpty()) {
            throw new CommandException(Format.error("WorldEdit is not installed on this server!"));
        }
        final ServerPlayer spongePlayer = CommandUtils.getServerPlayer(context);
        final Player wePlayer = SpongeAdapter.adapt(spongePlayer);
        final LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
        try {
            final Region region = session.getSelection(session.getSelectionWorld());
            return new Vector3iRange(adapt(region.getMinimumPoint()), adapt(region.getMaximumPoint()));
        } catch (final IncompleteRegionException e) {
            throw new CommandException(Format.error("Make a WorldEdit selection first!"), e);
        }
    }

    @Override
    public Optional<SelectorFlag> getFlag() {
        return Optional.of(SelectorFlag.of(Flag.of(Parameters.WORLDEDIT, "w"),
                Component.text("Use your WorldEdit selection as a region")));
    }

    private static Vector3i adapt(BlockVector3 worldedit) {
        return Vector3i.from(worldedit.x(), worldedit.y(), worldedit.z());
    }

}
