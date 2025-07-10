package net.slimediamond.espial.sponge.query.selector;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.Flag;

/**
 * A selector that a player can use
 */
public interface Selector {

    /**
     * Select two points (a region)
     *
     * @param context The command context which is selecting
     *                the region
     * @return The selected region
     */
    Vector3iRange select(@NotNull CommandContext context) throws CommandException;

    /**
     * Get the flag associated with this selector
     *
     * @return Selector flag
     */
    Flag getFlag();

    /**
     * Get the description for this selector
     *
     * @return Selector description
     */
    Component getDescription();

}
