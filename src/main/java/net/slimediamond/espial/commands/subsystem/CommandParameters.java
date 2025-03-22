package net.slimediamond.espial.commands.subsystem;

import net.slimediamond.espial.api.query.Sort;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandParameters {
    public static final Parameter.Value<Integer> LOOKUP_RANGE = Parameter.integerNumber().key("lookup range").build();
    public static final Parameter.Value<UUID> LOOKUP_PLAYER = Parameter.user().key("player").build();
    public static final Parameter.Value<BlockState> LOOKUP_BLOCK = Parameter.blockState().key("block type").build();
    public static final Parameter.Value<Integer> GENERIC_ID = Parameter.integerNumber().key("id").build();
    public static final Parameter.Value<String> TIME = Parameter.string().key("time").build();
    public static final Parameter.Value<Sort> SORT = Parameter.builder(Sort.class).key("sort")
            .addParser((parameterKey, reader, context) -> {
                String input = reader.parseString();
                try {
                    return Optional.of(Sort.valueOf(input.toUpperCase().replace(" ", "_")));
                } catch (IllegalArgumentException e) {
                    return Optional.empty();
                }
            })
            .completer((context, input) ->
                    Arrays.stream(Sort.values())
                            .map(Enum::name)
                            .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                            .map(CommandCompletion::of)
                            .collect(Collectors.toList())
            )
            .build();
}
