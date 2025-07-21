package net.slimediamond.espial.sponge.query.selector;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.Parameters;
import net.slimediamond.espial.common.utils.formatting.Format;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

public class RangeSelector implements Selector {

    @Override
    public Vector3iRange select(@NotNull final CommandContext context) throws CommandException {
        final int range = context.one(Parameters.RANGE).orElse(Espial.getInstance().getConfig().getNearRange());
        if (getFlag().isEmpty()) {
            context.sendMessage(Format.defaults("Range: " + range + " blocks"));
        }
        final Vector3i origin = context.cause().location()
                .orElseThrow(() -> new CommandException(Format.error("Only things with a location can run this")))
                .blockPosition();

        final Vector3i minimum = origin.sub(range, range, range);
        final Vector3i maximum = origin.add(range, range, range);

        return new Vector3iRange(minimum, maximum);
    }

    @Override
    public Optional<SelectorFlag> getFlag() {
        return Optional.of(SelectorFlag.of(Flag.of(Parameters.RANGE, "r"),
                Component.text("Select a given range of blocks around you as a cuboid")));
    }

}
