package net.slimediamond.espial.sponge.commands.subsystem;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.parameter.managed.Flag;

import java.util.Map;

public class Flags {

    public static final Flag SPREAD = Flag.of(Parameters.SPREAD, "s");
    public static final Flag AFTER = Flag.of(Parameters.AFTER, "t");
    public static final Flag BEFORE = Flag.of(Parameters.BEFORE, "before"); // 'b' is reserved for block
    public static final Flag PLAYER = Flag.of(Parameters.USER, "p");
    public static final Flag EVENT = Flag.of(Parameters.EVENT, "e");
    public static final Flag BLOCK = Flag.of(Parameters.BLOCK_TYPE, "b");
    public static final Flag YES = Flag.of(Parameters.YES, "yes");

    public static final Map<Flag, Component> SPREAD_FLAG = Map.of(SPREAD,
            Component.text("Spread results, showing individual ones"));

}
