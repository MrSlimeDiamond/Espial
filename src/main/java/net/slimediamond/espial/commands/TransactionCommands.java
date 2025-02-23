package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.CommandParameters;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.query.Sort;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import net.slimediamond.espial.sponge.transaction.BasicEspialTransaction;
import net.slimediamond.espial.util.ArgumentUtil;
import net.slimediamond.espial.util.Format;
import net.slimediamond.espial.util.PlayerSelectionUtil;
import net.slimediamond.espial.util.RayTraceUtil;
import net.slimediamond.espial.util.WorldEditSelectionUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionCommands {

    public static CommandResult execute(CommandContext context, QueryType type,
                                        Sort sort) {
        Player player;
        if (context.cause().root() instanceof Player) {
            player = (Player) context.cause().root();
        } else {
            return CommandResult.error(
                    Component.text("This command can only be used by players.")
                            .color(NamedTextColor.RED));
        }

        ArgumentUtil.Requirements args = ArgumentUtil.parse(context, type);
        if (!args.shouldContinue()) return CommandResult.success();

        Query.Builder builder = Query.builder()
                .type(type)
                .player(args.getUUID())
                .sort(sort)
                .caller(player)
                .spread(context.hasFlag("s"))
                .audience(player)
                .after(args.getTimestamp());

        if (args.getBlockId() != null) {
            builder.block(args.getBlockId());
        }

        if (context.hasFlag("worldedit")) { // Range lookup
            try {
                Optional<Pair<ServerLocation, ServerLocation>>
                        selectionOptional =
                        WorldEditSelectionUtil.getWorldEditRegion(player);

                if (selectionOptional.isPresent()) {
                    Pair<ServerLocation, ServerLocation> selection =
                            selectionOptional.get();
                    context.sendMessage(Format.text("Using your WorldEdit " +
                            "selection for this query."));
                    builder.min(selection.getLeft());
                    builder.max(selection.getRight());
                } else {
                    context.sendMessage(Format.error("You do not have a " +
                            "WorldEdit selection active."));
                    return CommandResult.success();
                }
            } catch (NoClassDefFoundError e) {
                context.sendMessage(Format.error("WorldEdit is not installed " +
                        "on this server."));
                return CommandResult.success();
            }

        } else if (context.hasFlag("range")) {
            // -r <block range>
            int range = context.requireOne(CommandParameters.LOOKUP_RANGE);

            Pair<ServerLocation, ServerLocation> selection =
                    PlayerSelectionUtil.getCuboidAroundPlayer(player, range);

            builder.min(selection.getLeft());
            builder.max(selection.getRight());
        } else {
            // Ray trace block (playing is looking at target)
            // get the block the player is targeting

            Optional<LocatableBlock> locatableBlock =
                    RayTraceUtil.getBlockFacingPlayer(player);

            if (locatableBlock.isPresent()) {
                LocatableBlock block = locatableBlock.get();

                builder.min(block.serverLocation());
            } else {
                context.sendMessage(Format.noBlockFound());
                return CommandResult.success();
            }
        }

        try {
            builder.build().submit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return CommandResult.success();
    }

    public static class Undo implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context)
                throws CommandException {
            try {
                int actions = Espial.getInstance().getTransactionManager()
                        .undo(context.cause().root());
                if (actions == 0) {
                    context.sendMessage(Format.text("Nothing was undone."));
                } else {
                    context.sendMessage(
                            Format.text(actions + " action(s) have " +
                                    "been undone."));
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return CommandResult.success();
        }
    }

    public static class Redo implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context)
                throws CommandException {
            try {
                int actions = Espial.getInstance().getTransactionManager()
                        .redo(context.cause().root());
                if (actions == 0) {
                    context.sendMessage(Format.text("Nothing was redone."));
                } else {
                    context.sendMessage(
                            Format.text(actions + " action(s) have " +
                                    "been redone."));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return CommandResult.success();
        }
    }

    public static class RollbackId implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context)
                throws CommandException {
            int id = context.requireOne(CommandParameters.GENERIC_ID);
            try {
                EspialRecord record =
                        Espial.getInstance().getDatabase().queryId(id);

                List<Integer> ids = new ArrayList<>();
                ids.add(id);

                TransactionStatus status = record.rollback();

                if (status == TransactionStatus.SUCCESS) {
                    context.sendMessage(Format.text(ids.size() + " action(s) " +
                            "have been rolled back."));

                    Espial.getInstance().getTransactionManager()
                            .add(context.cause().root(),
                                    new BasicEspialTransaction(
                                            QueryType.RESTORE,
                                            context.cause().root(),
                                            context.cause().audience(), ids));
                } else if (status == TransactionStatus.ALREADY_DONE) {
                    context.sendMessage(Format.error("That action has " +
                            "already been rolled back."));
                } else if (status == TransactionStatus.UNSUPPORTED) {
                    context.sendMessage(Format.error("Rolling back this " +
                            "action is not supported."));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return CommandResult.success();
        }
    }

    public static class RestoreId implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context)
                throws CommandException {
            int id = context.requireOne(CommandParameters.GENERIC_ID);
            try {
                EspialRecord record =
                        Espial.getInstance().getDatabase().queryId(id);

                List<Integer> ids = new ArrayList<>();
                ids.add(id);

                TransactionStatus status = record.restore();

                if (status == TransactionStatus.SUCCESS) {
                    context.sendMessage(Format.text(ids.size() + " action(s) " +
                            "have been restored."));

                    Espial.getInstance().getTransactionManager()
                            .add(context.cause().root(),
                                    new BasicEspialTransaction(
                                            QueryType.RESTORE,
                                            context.cause().root(),
                                            context.cause().audience(), ids));
                } else if (status == TransactionStatus.ALREADY_DONE) {
                    context.sendMessage(Format.error("That action has " +
                            "already been restored."));
                } else if (status == TransactionStatus.UNSUPPORTED) {
                    context.sendMessage(Format.error("Restoring this " +
                            "action is not supported."));
                }


            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return CommandResult.success();
        }
    }

    public static class Lookup implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context)
                throws CommandException {
            return TransactionCommands.execute(context, QueryType.LOOKUP,
                    Sort.REVERSE_CHRONOLOGICAL);
        }
    }

    public static class Rollback implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context)
                throws CommandException {
            return TransactionCommands.execute(context, QueryType.ROLLBACK,
                    Sort.REVERSE_CHRONOLOGICAL);
        }
    }

    public static class Restore implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context)
                throws CommandException {
            return TransactionCommands.execute(context, QueryType.RESTORE,
                    Sort.CHRONOLOGICAL);
        }
    }
}
