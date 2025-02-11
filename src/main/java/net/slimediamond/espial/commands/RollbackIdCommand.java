package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.*;
import net.slimediamond.espial.action.BlockAction;
import net.slimediamond.espial.transaction.TransactionStatus;
import net.slimediamond.espial.transaction.EspialTransaction;
import net.slimediamond.espial.transaction.EspialTransactionType;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.sql.SQLException;
import java.util.ArrayList;

public class RollbackIdCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        // rollback <id>
        int id = context.requireOne(CommandParameters.ROLLBACK_ID);

        try {
            BlockAction action = Espial.getInstance().getDatabase().queryId(id);
            TransactionStatus status = Espial.getInstance().getBlockLogService().rollback(action);

            if (status == TransactionStatus.SUCCESS) {
                context.sendMessage(Component.text()
                        .append(Espial.prefix)
                        .append(Component.text("Reversal successful. Griefers beware!").color(NamedTextColor.WHITE)
                        ).build()
                );

                ArrayList<Integer> ids = new ArrayList<>();
                ids.add(id);
                EspialTransaction transaction = new EspialTransaction(ids, EspialTransactionType.ROLLBACK, false);

                Espial.getInstance().getBlockLogService().addTransaction(context.cause().root(), transaction);

                return CommandResult.success();
            } else if (status == TransactionStatus.UNSUPPORTED) {
                context.sendMessage(Component.text()
                        .append(Espial.prefix)
                        .append(Component.text("That operation is not supported at the moment!").color(NamedTextColor.RED)
                        ).build()
                );

                return CommandResult.success();
            } else if (status == TransactionStatus.ALREADY_DONE) {
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
