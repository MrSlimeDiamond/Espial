package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.*;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.sql.SQLException;
import java.util.ArrayList;

public class RestoreIdCommand implements CommandExecutor {
    private Database database;

    public RestoreIdCommand(Database database) {
        this.database = database;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        // restore <id>
        int id = context.requireOne(Parameters.ROLLBACK_ID);

        try {
            StoredBlock block = database.queryId(id);
            RestoreStatus status = Espial.getInstance().restore(block);

            if (status == RestoreStatus.SUCCESS) {
                context.sendMessage(Component.text()
                        .append(Espial.prefix)
                        .append(Component.text("Restore successful. Griefers beware!").color(NamedTextColor.WHITE)
                        ).build()
                );

                ArrayList<Integer> ids = new ArrayList<>();
                ids.add(id);
                EspialTransaction transaction = new EspialTransaction(ids, EspialTransactionType.RESTORE, false);

                if (Espial.transactions.containsKey(context.cause().root())) {
                    // add to the existing arraylist with a new transaction:
                    Espial.transactions.get(context.cause().root()).add(transaction);
                } else {
                    // create a new one with the source object
                    ArrayList<EspialTransaction> transactions = new ArrayList<>();
                    transactions.add(transaction);

                    Espial.transactions.put(context.cause().root(), transactions);
                }

                return CommandResult.success();
            } else if (status == RestoreStatus.UNSUPPORTED) {
                context.sendMessage(Component.text()
                        .append(Espial.prefix)
                        .append(Component.text("That operation is not supported at the moment!").color(NamedTextColor.RED)
                        ).build()
                );

                return CommandResult.success();
            } else if (status == RestoreStatus.ALREADY_DONE) {
                context.sendMessage(Component.text()
                        .append(Espial.prefix)
                        .append(Component.text("That id has already been restored!").color(NamedTextColor.RED)
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
