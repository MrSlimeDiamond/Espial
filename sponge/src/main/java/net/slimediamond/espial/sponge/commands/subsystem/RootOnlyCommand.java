package net.slimediamond.espial.sponge.commands.subsystem;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.sponge.permission.Permission;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class RootOnlyCommand extends AbstractCommand {

    /**
     * Constructor for a command
     *
     * @param permission  Command permission
     * @param description Command description
     */
    public RootOnlyCommand(@Nullable final Permission permission, @NotNull final Component description) {
        super(permission, description);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException {
        return new HelpCommand(this).execute(context);
    }

}
