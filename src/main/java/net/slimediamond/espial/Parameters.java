package net.slimediamond.espial;

import org.spongepowered.api.command.parameter.Parameter;

public class Parameters {
    public static Parameter.Value<Integer> LOOKUP_RANGE = Parameter.integerNumber().key("lookup range").build();
}
