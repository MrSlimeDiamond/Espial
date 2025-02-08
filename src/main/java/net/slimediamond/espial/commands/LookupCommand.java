package net.slimediamond.espial.commands;

import net.slimediamond.espial.*;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;

public class LookupCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandContext context) {
        return Espial.getInstance().getBlockLogService().doSelectiveCommand(context, EspialTransactionType.LOOKUP);
    }
}
