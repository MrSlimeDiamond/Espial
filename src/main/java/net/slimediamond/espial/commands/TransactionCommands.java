package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.CommandParameters;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.util.ArrayList;
import java.util.List;

public class TransactionCommands {

    public static class Undo implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context) throws CommandException {
            try {
                int actions = Espial.getInstance().getTransactionManager().undo(context.cause().root());
                context.sendMessage(Espial.prefix.append(Component.text(actions + " action(s) have been undone.").color(NamedTextColor.WHITE)));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return CommandResult.success();
        }
    }

    public static class Redo implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context) throws CommandException {
            try {
                int actions = Espial.getInstance().getTransactionManager().redo(context.cause().root());
                context.sendMessage(Espial.prefix.append(Component.text(actions + " action(s) have been redone").color(NamedTextColor.WHITE)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return CommandResult.success();
        }
    }

    public static class RollbackId implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context) throws CommandException {
            int id = context.requireOne(CommandParameters.GENERIC_ID);
            try {
                BlockAction action = Espial.getInstance().getDatabase().queryId(id);

                List<Integer> ids = new ArrayList<>();
                ids.add(id);
                Query query = Query.builder()
                        .setType(QueryType.ROLLBACK)
                        .setMin(action.getServerLocation())
                        .setUser(context.cause().root())
                        .setAudience(context.cause().audience())
                        .build();

                Espial.getInstance().getEspialService().submit(query);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return CommandResult.success();
        }
    }

    public static class RestoreId implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context) throws CommandException {
            int id = context.requireOne(CommandParameters.GENERIC_ID);
            try {
                BlockAction action = Espial.getInstance().getDatabase().queryId(id);

                List<Integer> ids = new ArrayList<>();
                ids.add(id);
                Query query = Query.builder()
                        .setType(QueryType.RESTORE)
                        .setMin(action.getServerLocation())
                        .setUser(context.cause().root())
                        .setAudience(context.cause().audience())
                        .build();

                Espial.getInstance().getEspialService().submit(query);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return CommandResult.success();
        }
    }
}
