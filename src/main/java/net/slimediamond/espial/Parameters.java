package net.slimediamond.espial;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.UUID;

public class Parameters {
    public static Parameter.Value<Integer> LOOKUP_RANGE = Parameter.integerNumber().key("lookup range").build();
    public static Parameter.Value<UUID> LOOKUP_PLAYER = Parameter.user().key("player").build();
    public static Parameter.Value<BlockState> LOOKUP_BLOCK = Parameter.blockState().key("block type").build();
    public static Parameter.Value<String> HELP_COMMAND = Parameter.string().key("subcommand").optional().build();
}
