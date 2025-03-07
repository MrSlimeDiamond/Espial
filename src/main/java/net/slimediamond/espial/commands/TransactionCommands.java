package net.slimediamond.espial.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.query.Sort;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.BasicEspialTransaction;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import net.slimediamond.espial.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.commands.subsystem.CommandParameters;
import net.slimediamond.espial.util.ArgumentUtil;
import net.slimediamond.espial.util.Format;
import net.slimediamond.espial.util.PlayerSelectionUtil;
import net.slimediamond.espial.util.RayTraceUtil;
import net.slimediamond.espial.util.WorldEditSelectionUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;

public class TransactionCommands {

  private static final Map<Flag, Component> flags =
      Map.of(
          Flag.builder().aliases("range", "r").setParameter(CommandParameters.LOOKUP_RANGE).build(),
              Component.text("The range to look up"),
          Flag.builder().aliases("worldedit", "we", "w").setParameter(Parameter.bool().key("use worldedit").optional().build()).build(),
              Component.text("Whether to use WorldEdit"),
          Flag.builder().aliases("player", "p").setParameter(CommandParameters.LOOKUP_PLAYER).build(),
              Component.text("Filter by a specific player"),
          Flag.builder().aliases("block", "b").setParameter(CommandParameters.LOOKUP_BLOCK).build(),
              Component.text("Filter by a specific block"),
          Flag.builder().aliases("time", "after", "t").setParameter(CommandParameters.TIME).build(),
              Component.text("Set a time to query after"),
          Flag.builder().aliases("spread", "singe", "s").setParameter(Parameter.bool().key("spread").optional().build()).build(),
              Component.text("Show individual events")
      );

  private TransactionCommands() {}

  private static CommandResult execute(CommandContext context, QueryType type, Sort sort) {
    Player player;
    if (context.cause().root() instanceof Player) {
      player = (Player) context.cause().root();
    } else {
      return CommandResult.error(
          Component.text("This command can only be used by players.").color(NamedTextColor.RED));
    }

    ArgumentUtil.Requirements args = ArgumentUtil.parse(context, type);
    if (!args.shouldContinue()) return CommandResult.success();

    Query.Builder builder =
        Query.builder()
            .type(type)
            .players(args.getUUIDs())
            .sort(sort)
            .caller(player)
            .spread(context.hasFlag("s"))
            .audience(player)
            .after(args.getTimestamp());

    if (args.getBlocks() != null) {
      builder.blocks(args.getBlocks());
    }

    if (context.hasFlag("worldedit")) { // Range lookup
      try {
        Optional<Pair<ServerLocation, ServerLocation>> selectionOptional =
            WorldEditSelectionUtil.getWorldEditRegion(player);

        if (selectionOptional.isPresent()) {
          Pair<ServerLocation, ServerLocation> selection = selectionOptional.get();
          context.sendMessage(Format.text("Using your WorldEdit selection for this query."));
          builder.min(selection.getLeft());
          builder.max(selection.getRight());
        } else {
          context.sendMessage(Format.error("You do not have a WorldEdit selection active."));
          return CommandResult.success();
        }
      } catch (NoClassDefFoundError e) {
        context.sendMessage(Format.error("WorldEdit is not installed on this server."));
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

      Optional<LocatableBlock> locatableBlock = RayTraceUtil.getBlockFacingPlayer(player);

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

  public static class Undo extends AbstractCommand {
    /** Constructor for a command */
    Undo() {
      super("espial.command.undo", Component.text("Undo your previous " + "transactions"));
      addAlias("undo");
      addFlags(flags);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
      try {
        int actions = Espial.getInstance().getTransactionManager().undo(context.cause().root());
        if (actions == 0) {
          context.sendMessage(Format.text("Nothing was undone."));
        } else {
          context.sendMessage(Format.text(actions + " action(s) have been undone."));
        }

      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      return CommandResult.success();
    }
  }

  public static class Redo extends AbstractCommand {
    Redo() {
      super("espial.command.redo", Component.text("Redo your previous undone " + "transactions"));
      addAlias("redo");
      addFlags(flags);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
      try {
        int actions = Espial.getInstance().getTransactionManager().redo(context.cause().root());
        if (actions == 0) {
          context.sendMessage(Format.text("Nothing was redone."));
        } else {
          context.sendMessage(Format.text(actions + " action(s) have been redone."));
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      return CommandResult.success();
    }
  }

  public static class RollbackId extends AbstractCommand {
    RollbackId() {
      super(
          "espial.command.rollbackid",
          Component.text("Roll back a record using its internal ID"));
      addAlias("rollbackid");
      addAlias("rbid");
      addParameter(CommandParameters.GENERIC_ID);
      showInHelp(false);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
      int id = context.requireOne(CommandParameters.GENERIC_ID);
      try {
        EspialRecord record = Espial.getInstance().getDatabase().queryId(id);

        List<Integer> ids = new ArrayList<>();
        ids.add(id);

        TransactionStatus status = record.rollback();

        if (status == TransactionStatus.SUCCESS) {
          context.sendMessage(Format.text(ids.size() + " action(s) have been rolled back."));

          Espial.getInstance()
              .getTransactionManager()
              .add(
                  context.cause().root(),
                  new BasicEspialTransaction(
                      QueryType.RESTORE, context.cause().root(), context.cause().audience(), ids));
        } else if (status == TransactionStatus.ALREADY_DONE) {
          context.sendMessage(Format.error("That action has already been rolled back."));
        } else if (status == TransactionStatus.UNSUPPORTED) {
          context.sendMessage(Format.error("Rolling back this action is not supported."));
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      return CommandResult.success();
    }
  }

  public static class RestoreId extends AbstractCommand {
    RestoreId() {
      super(
          "espial.command.restoreid",
          Component.text("Restore a record using its internal ID"));
      addAlias("restoreid");
      addAlias("rsid");
      addParameter(CommandParameters.GENERIC_ID);
      showInHelp(false);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
      int id = context.requireOne(CommandParameters.GENERIC_ID);
      try {
        EspialRecord record = Espial.getInstance().getDatabase().queryId(id);

        List<Integer> ids = new ArrayList<>();
        ids.add(id);

        TransactionStatus status = record.restore();

        if (status == TransactionStatus.SUCCESS) {
          context.sendMessage(Format.text(ids.size() + " action(s) have been restored."));

          Espial.getInstance()
              .getTransactionManager()
              .add(
                  context.cause().root(),
                  new BasicEspialTransaction(
                      QueryType.RESTORE, context.cause().root(), context.cause().audience(), ids));
        } else if (status == TransactionStatus.ALREADY_DONE) {
          context.sendMessage(Format.error("That action has already been restored."));
        } else if (status == TransactionStatus.UNSUPPORTED) {
          context.sendMessage(Format.error("Restoring this action is not supported."));
        }

      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      return CommandResult.success();
    }
  }

  public static class Lookup extends AbstractCommand {
    Lookup() {
      super("espial.command.lookup", Component.text("Send a lookup query to " + "view logs"));
      addAlias("lookup");
      addAlias("l");
      addFlags(flags);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
      return TransactionCommands.execute(context, QueryType.LOOKUP, Sort.REVERSE_CHRONOLOGICAL);
    }
  }

  public static class Rollback extends AbstractCommand {
    Rollback() {
      super(
          "espial.command.rollback",
          Component.text("Send a rollback " + "request to bring blocks back in time"));
      addAlias("rollback");
      addAlias("rb");
      addFlags(flags);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
      return TransactionCommands.execute(context, QueryType.ROLLBACK, Sort.REVERSE_CHRONOLOGICAL);
    }
  }

  public static class Restore extends AbstractCommand {
    Restore() {
      super(
          "espial.command.restore",
          Component.text("Send a restore request to restore undone " +
                  "changes"));
      addAlias("restore");
      addAlias("rs");
      addFlags(flags);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
      return TransactionCommands.execute(context, QueryType.RESTORE, Sort.CHRONOLOGICAL);
    }
  }

  public static class Near extends AbstractCommand {
    Near() {
      super("espial.command.lookup", Component.text("Lookup blocks nearby"));
      addAlias("near");
      addFlags(flags);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
      // TODO: Make this better, probably make a requirements class or something
      if (context.subject() instanceof Player player) {
        int range = Espial.getInstance().getConfig().get().getDefaultLookupRange();
        if (context.hasFlag("r")) {
          range = context.requireOne(CommandParameters.LOOKUP_RANGE);
        } else {
          player.sendMessage(Format.defaults("-r " + range));
        }

        Pair<ServerLocation, ServerLocation> selection =
                PlayerSelectionUtil.getCuboidAroundPlayer(player, range);

        ArgumentUtil.Requirements requirements =
                ArgumentUtil.parse(context, QueryType.LOOKUP);

        if (!requirements.shouldContinue()) return CommandResult.success();

        Query.Builder builder =
                Query.builder()
                        .min(selection.getLeft())
                        .max(selection.getRight())
                        .players(requirements.getUUIDs())
                        .spread(context.hasFlag("s"))
                        .after(requirements.getTimestamp())
                        .audience(context.cause().audience())
                        .caller(player)
                        .sort(Sort.REVERSE_CHRONOLOGICAL)
                        .type(QueryType.LOOKUP);

        if (requirements.getBlocks() != null) {
          builder.blocks(requirements.getBlocks());
        }

        try {
          builder.build().submit();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      } else {
        context.sendMessage(Format.playersOnly());
      }
      return CommandResult.success();
    }
  }
}
