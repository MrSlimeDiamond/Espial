package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.CommandParameters;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.query.Sort;
import net.slimediamond.espial.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TransactionCommands {

    public static CommandResult execute(CommandContext context, QueryType type, Sort sort) {
        Player player;
        if (context.cause().root() instanceof Player) {
            player = (Player) context.cause().root();
        } else {
            return CommandResult.error(Component.text("This command can only be used by players.").color(NamedTextColor.RED));
        }

        Timestamp timestamp;
        try {
            timestamp = parseTimestamp(context, type);
        } catch (IllegalArgumentException e) {
            context.sendMessage(Espial.prefix.append(Component.text().append(Component.text("Could not parse time argument '").append(Component.text(context.requireOne(CommandParameters.TIME)).append(Component.text("'.")))).color(NamedTextColor.RED)));
            return CommandResult.success();
        }
        UUID uuid = parseFilter(context, "player", CommandParameters.LOOKUP_PLAYER);

        Query.Builder builder = Query.builder()
                .setType(type)
                .setPlayerUUID(uuid)
                .setSort(sort)
                .setUser(player)
                .setSpread(context.hasFlag("s"))
                .setAudience(player)
                .setTimestamp(timestamp);

        BlockState blockState = parseFilter(context, "block", CommandParameters.LOOKUP_BLOCK);
        if (blockState != null) {
            String blockId = RegistryTypes.BLOCK_TYPE.get().valueKey(blockState.type()).formatted();
            builder.setBlockId(blockId);
        }

        if (context.hasFlag("worldedit")) { // Range lookup
            try {
                Optional<Pair<ServerLocation, ServerLocation>> selectionOptional = WorldEditSelectionUtil.getWorldEditRegion(player);

                if (selectionOptional.isPresent()) {
                    Pair<ServerLocation, ServerLocation> selection = selectionOptional.get();
                    context.sendMessage(Component.text().append(Espial.prefix).append(Component.text("Using your WorldEdit selection for this query.").color(NamedTextColor.WHITE)).build());
                    builder.setMin(selection.getLeft());
                    builder.setMax(selection.getRight());
                } else {
                    context.sendMessage(Espial.prefix.append(Component.text("You do not have a WorldEdit selection active!").color(NamedTextColor.RED)));
                    return CommandResult.success();
                }
            } catch (NoClassDefFoundError e) {
                context.sendMessage(Component.text("It doesn't look like WorldEdit is installed on this server!").color(NamedTextColor.RED));
                return CommandResult.success();
            }

        } else if (context.hasFlag("range")) {
            // -r <block range>
            int range = context.requireOne(CommandParameters.LOOKUP_RANGE);

            Pair<ServerLocation, ServerLocation> selection = PlayerSelectionUtil.getCuboidAroundPlayer(player, range);

            context.sendMessage(Component.text().append(Espial.prefix).append(Component.text("Using a cuboid with a range of " + range + " blocks for this query.").color(NamedTextColor.WHITE)).build());

            builder.setMin(selection.getLeft());
            builder.setMax(selection.getRight());
        } else {
            // Ray trace block (playing is looking at target)
            // get the block the player is targeting

            Optional<LocatableBlock> result = RayTraceUtil.getBlockFacingPlayer(player);

            if (result.isPresent()) {
                LocatableBlock block = result.get();

                builder.setMin(block.serverLocation());
            } else {
                context.sendMessage(Espial.prefix.append(Component.text("Could not detect a block. Move closer, perhaps?").color(NamedTextColor.RED)));
                return CommandResult.success();
            }
        }

        try {
            Espial.getInstance().getEspialService().submit(builder.build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return CommandResult.success();
    }

    private static <T> T parseFilter(CommandContext context, String flag, Parameter.Value<T> parameter) {
        return context.hasFlag(flag) ? context.requireOne(parameter) : null;
    }

    private static Timestamp parseTimestamp(CommandContext context, QueryType type) {
        if (context.hasFlag("time")) {
            String time = context.requireOne(CommandParameters.TIME);
            return new Timestamp(DurationParser.parseDurationAndSubtract(time));
        }
        if (type != QueryType.LOOKUP) {
            context.sendMessage(Espial.prefix.append(Component.text("Defaults used: -t 3d").color(NamedTextColor.GRAY)));
            return Timestamp.from(Instant.now().minus(3, ChronoUnit.DAYS));
        } else return Timestamp.from(Instant.ofEpochMilli(0)); // gotta catch 'em all!
    }

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

    public static class Lookup implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context) throws CommandException {
            return TransactionCommands.execute(context, QueryType.LOOKUP, Sort.REVERSE_CHRONOLOGICAL);
        }
    }

    public static class Rollback implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context) throws CommandException {
            return TransactionCommands.execute(context, QueryType.ROLLBACK, Sort.REVERSE_CHRONOLOGICAL);
        }
    }

    public static class Restore implements CommandExecutor {
        @Override
        public CommandResult execute(CommandContext context) throws CommandException {
            return TransactionCommands.execute(context, QueryType.RESTORE, Sort.CHRONOLOGICAL);
        }
    }
}
