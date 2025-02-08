package net.slimediamond.espial.commands;

import net.slimediamond.espial.*;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class RollbackCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        return Espial.getInstance().getBlockLogService().doSelectiveCommand(context, EspialTransactionType.ROLLBACK);
    }
}
