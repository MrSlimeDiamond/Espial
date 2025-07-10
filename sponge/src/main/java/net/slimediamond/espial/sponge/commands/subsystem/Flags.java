package net.slimediamond.espial.sponge.commands.subsystem;

import org.spongepowered.api.command.parameter.managed.Flag;

public class Flags {

    public static final Flag SPREAD = Flag.of(Parameters.SPREAD, "spread", "s");
    public static final Flag AFTER = Flag.of(Parameters.AFTER, "after", "a", "t");
    public static final Flag BEFORE = Flag.of(Parameters.BEFORE, "before"); // 'b' is reserved for block
    public static final Flag PLAYER = Flag.of(Parameters.USER, "player", "p");
    public static final Flag EVENT = Flag.of(Parameters.EVENT, "event", "e");
    public static final Flag BLOCK = Flag.of(Parameters.BLOCK_TYPE, "block", "b");
    public static final Flag WORLDEDIT = Flag.of(Parameters.WORLDEDIT, "worldedit", "w");
    public static final Flag YES = Flag.of(Parameters.YES, "yes");

}
