package net.slimediamond.espial.sponge.query.selector;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.sponge.commands.subsystem.Parameters;
import net.slimediamond.espial.common.utils.formatting.Format;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.math.vector.Vector3i;

public class RangeSelector implements Selector {

    @Override
    public Vector3iRange select(@NotNull final CommandContext context) throws CommandException {
        final int range = context.requireOne(Parameters.RANGE);
        final Vector3i origin = context.cause().location()
                .orElseThrow(() -> new CommandException(Format.error("Only things with a location can run this")))
                .blockPosition();

        final Vector3i minimum = origin.sub(range, range, range);
        final Vector3i maximum = origin.add(range, range, range);

        return new Vector3iRange(minimum, maximum);
    }

    @Override
    public Flag getFlag() {
        return Flag.of(Parameters.RANGE, "range", "r");
    }

    @Override
    public Component getDescription() {
        return Component.text("Select a given range of blocks around you as a cuboid");
    }

}
