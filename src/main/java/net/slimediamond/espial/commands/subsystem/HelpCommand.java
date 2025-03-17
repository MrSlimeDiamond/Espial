package net.slimediamond.espial.commands.subsystem;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class HelpCommand extends AbstractCommand {
    private final AbstractCommand parent;

    HelpCommand(AbstractCommand parent) {
        super(parent.getPermission(), Component.text("Help subcommand"));
        this.parent = parent;

        addAlias("help");
        addAlias("?");
        showInHelp(false);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        parent.sendHelpCommand(context.cause());
        return CommandResult.success();
    }
}
