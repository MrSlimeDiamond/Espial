package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.*;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.transaction.EspialTransaction;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import net.slimediamond.espial.sponge.transaction.EspialTransactionImpl;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.util.ArrayList;
import java.util.List;

public class RestoreIdCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        // restore <id>
        int id = context.requireOne(CommandParameters.GENERIC_ID);

        try {
            BlockAction action = Espial.getInstance().getDatabase().queryId(id);
            TransactionStatus status = Espial.getInstance().getEspialService().restore(action);

            if (status == TransactionStatus.SUCCESS) {
                context.sendMessage(Component.text()
                        .append(Espial.prefix)
                        .append(Component.text("Restore successful. Griefers beware!").color(NamedTextColor.WHITE)
                        ).build()
                );

                List<Integer> ids = new ArrayList<>();
                ids.add(id);

                Query query = Query.builder()
                        .setType(QueryType.RESTORE)
                        .setMin(action.getServerLocation())
                        .build();

                EspialTransaction transaction = new EspialTransactionImpl(ids, query, context.cause().root(), context.cause().audience());
                Espial.getInstance().getEspialService().submit(transaction);

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
