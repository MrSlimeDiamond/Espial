package net.slimediamond.espial.util;

import net.slimediamond.espial.CommandParameters;
import net.slimediamond.espial.api.query.QueryType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.registry.RegistryTypes;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class ArgumentUtil {

    public static Result parse(CommandContext context, QueryType type) {
        UUID uuid =
                parseFilter(context, "player", CommandParameters.LOOKUP_PLAYER);

        BlockState blockState =
                parseFilter(context, "block", CommandParameters.LOOKUP_BLOCK);

        Timestamp timestamp;
        try {
            timestamp = parseTimestamp(context, type);
        } catch (IllegalArgumentException e) {
            context.sendMessage(Format.error("Could not parse time argument " +
                    "'" + context.requireOne(CommandParameters.TIME) + "'."));
            return new Result(null, null, null, false);
        }

        String blockId = null;

        if (blockState != null) {
            blockId =
                RegistryTypes.BLOCK_TYPE.get().valueKey(blockState.type())
                        .formatted();
        }

        return new Result(timestamp, uuid, blockId, true);
    }

    private static <T> T parseFilter(CommandContext context, String flag,
                                     Parameter.Value<T> parameter) {
        return context.hasFlag(flag) ? context.requireOne(parameter) : null;
    }

    private static Timestamp parseTimestamp(CommandContext context,
                                            QueryType type) {
        if (context.hasFlag("time")) {
            String time = context.requireOne(CommandParameters.TIME);
            return new Timestamp(DurationParser.parseDurationAndSubtract(time));
        }
        if (type != QueryType.LOOKUP) {
            context.sendMessage(Format.defaults("-t 3d"));
            return Timestamp.from(Instant.now().minus(3, ChronoUnit.DAYS));
        } else {
            return Timestamp.from(
                    Instant.ofEpochMilli(0)); // gotta catch 'em all!
        }
    }

    public static final class Result {
        private Timestamp timestamp;
        private UUID uuid;
        private String blockId;
        private boolean shouldContinue;

        public Result(Timestamp timestamp, UUID uuid, String blockId, boolean shouldContinue) {
            this.timestamp = timestamp;
            this.uuid = uuid;
            this.blockId = blockId;
            this.shouldContinue = shouldContinue;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public UUID getUUID() {
            return uuid;
        }

        public String getBlockId() {
            return blockId;
        }

        public boolean shouldContinue() {
            return shouldContinue;
        }
    }
}
