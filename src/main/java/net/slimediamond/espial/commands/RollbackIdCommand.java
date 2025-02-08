package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.*;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.sql.SQLException;

public class RollbackIdCommand implements CommandExecutor {
    private Database database;

    public RollbackIdCommand(Database database) {
        this.database = database;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        // rollback <id>
        int id = context.requireOne(Parameters.ROLLBACK_ID);

        try {
            StoredBlock block = database.queryId(id);
            RollbackStatus status = Espial.getInstance().rollback(block);

            if (status == RollbackStatus.SUCCESS) {
                context.sendMessage(Component.text()
                        .append(Espial.prefix)
                        .append(Component.text("Reversal successful. Griefers beware!").color(NamedTextColor.WHITE)
                        ).build()
                );

                return CommandResult.success();
            } else if (status == RollbackStatus.UNSUPPORTED) {
                context.sendMessage(Component.text()
                        .append(Espial.prefix)
                        .append(Component.text("That operation is not supported at the moment!").color(NamedTextColor.RED)
                        ).build()
                );

                return CommandResult.success();
            } else if (status == RollbackStatus.ALREADY_ROLLEDBACK) {
                context.sendMessage(Component.text()
                        .append(Espial.prefix)
                        .append(Component.text("That id has already been rolled back!").color(NamedTextColor.RED)
                        ).build()
                );

                return CommandResult.success();
            } else {
                context.sendMessage(Component.text()
                        .append(Espial.prefix)
                        .append(Component.text("An unexpected error occurred!").color(NamedTextColor.RED)
                        ).build()
                );

                return CommandResult.success();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
