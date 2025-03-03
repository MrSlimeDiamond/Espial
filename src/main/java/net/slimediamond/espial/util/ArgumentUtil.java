package net.slimediamond.espial.util;

import net.slimediamond.espial.CommandParameters;
import net.slimediamond.espial.api.query.QueryType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.registry.RegistryTypes;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ArgumentUtil {

  public static Requirements parse(CommandContext context, QueryType type) {
    List<UUID> uuids = Collections.emptyList();
    List<BlockState> blocks = Collections.emptyList();
    if (context.hasFlag("player")) {
      uuids = (List<UUID>) context.all(CommandParameters.LOOKUP_PLAYER).stream().toList();
    }

    if (context.hasFlag("block")) {
      blocks = (List<BlockState>) context.all(CommandParameters.LOOKUP_BLOCK);
    }

    Timestamp timestamp;
    try {
      timestamp = parseTimestamp(context, type);
    } catch (IllegalArgumentException e) {
      context.sendMessage(
          Format.error(
              "Could not parse time argument "
                  + "'"
                  + context.requireOne(CommandParameters.TIME)
                  + "': "
                  + e.getMessage()));
      return new Requirements(null, null, null, false);
    }

    List<String> blockIds =
            blocks.stream().map(block -> RegistryTypes.BLOCK_TYPE.get().valueKey(block.type()).formatted()).toList();


    return new Requirements(timestamp, uuids, blockIds, true);
  }

  private static Timestamp parseTimestamp(CommandContext context, QueryType type) {
    if (context.hasFlag("time")) {
      String time = context.requireOne(CommandParameters.TIME);
      return new Timestamp(DurationParser.parseDurationAndSubtract(time));
    }
    if (type != QueryType.LOOKUP) {
      context.sendMessage(Format.defaults("-t 3d"));
      return Timestamp.from(Instant.now().minus(3, ChronoUnit.DAYS));
    } else {
      return Timestamp.from(Instant.ofEpochMilli(0)); // gotta catch 'em all!
    }
  }

  public static final class Requirements {
    private Timestamp timestamp;
    private List<UUID> uuids;
    private List<String> blocks;
    private boolean shouldContinue;

    public Requirements(Timestamp timestamp, List<UUID> uuids,
                        List<String> blocks,
                        boolean shouldContinue) {
      this.timestamp = timestamp;
      this.uuids = uuids;
      this.blocks = blocks;
      this.shouldContinue = shouldContinue;
    }

    public Timestamp getTimestamp() {
      return timestamp;
    }

    public List<UUID> getUUIDs() {
      return uuids;
    }

    public List<String> getBlocks() {
      return blocks;
    }

    public boolean shouldContinue() {
      return shouldContinue;
    }
  }
}
