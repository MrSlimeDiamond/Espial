package net.slimediamond.espial.sponge.query.selector;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.sponge.commands.subsystem.Parameters;
import net.slimediamond.espial.sponge.utils.CommandUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

public class GlobalSelector implements Selector {

    @Override
    public Vector3iRange select(@NotNull final CommandContext context) throws CommandException {
        // a bit scuffed. Select everything in the player's current world
        final ServerPlayer player = CommandUtils.getServerPlayer(context);
        final Vector3i max = player.world().size();
        final Vector3i min = Vector3i.from(-max.x(), -max.y(), -max.z());
        return new Vector3iRange(min, max);
    }

    @Override
    public Optional<SelectorFlag> getFlag() {
        return Optional.of(SelectorFlag.of(Flag.of(Parameters.GLOBAL, "g"),
                Component.text("Select a global region")));
    }

}
