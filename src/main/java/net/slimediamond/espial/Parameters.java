package net.slimediamond.espial;

import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.UUID;

public class Parameters {
    public static Parameter.Value<Integer> LOOKUP_RANGE = Parameter.integerNumber().key("lookup range").build();
    public static Parameter.Value<UUID> LOOKUP_PLAYER = Parameter.user().key("player").build();
}
