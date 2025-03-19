package net.slimediamond.espial.commands.subsystem;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.UUID;

public class CommandParameters {
    public static final Parameter.Value<Integer> LOOKUP_RANGE = Parameter.integerNumber().key("lookup range").build();
    public static final Parameter.Value<UUID> LOOKUP_PLAYER = Parameter.user().key("player").build();
    public static final Parameter.Value<BlockState> LOOKUP_BLOCK = Parameter.blockState().key("block type").build();
    public static final Parameter.Value<Integer> GENERIC_ID = Parameter.integerNumber().key("id").build();
    public static final Parameter.Value<String> TIME = Parameter.string().key("time").build();
}
