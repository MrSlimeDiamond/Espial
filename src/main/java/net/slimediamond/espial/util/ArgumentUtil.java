package net.slimediamond.espial.util;

import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.commands.subsystem.CommandParameters;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.registry.RegistryTypes;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ArgumentUtil {

    public static Requirements parse(CommandContext context, QueryType type) {
        List<UUID> uuids = Collections.emptyList();
        List<BlockState> blocks = Collections.emptyList();
        if (context.hasFlag("player")) {
            uuids = context.all(CommandParameters.LOOKUP_PLAYER).stream().collect(Collectors.toUnmodifiableList());
        }

        if (context.hasFlag("block")) {
            blocks = context.all(CommandParameters.LOOKUP_BLOCK).stream().collect(Collectors.toUnmodifiableList());
        }

        Timestamp timestamp;
        try {
            timestamp = parseTimestamp(context, type);
        } catch (IllegalArgumentException e) {
            context.sendMessage(Format.error(
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
            String defaultTime = Espial.getInstance().getConfig().get().getDefaultTime();
            context.sendMessage(Format.defaults("-t " + defaultTime));
            return Timestamp.from(Instant.ofEpochMilli(DurationParser.parseDurationAndSubtract(defaultTime)));
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
