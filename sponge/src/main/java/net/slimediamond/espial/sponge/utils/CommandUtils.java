package net.slimediamond.espial.sponge.utils;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class CommandUtils {

    public static ServerPlayer getServerPlayer(@NotNull final CommandContext context) throws CommandException {
        if (!(context.cause().subject() instanceof ServerPlayer player)) {
            throw new CommandException(Component.text("Only players can run this"));
        }
        return player;
    }

}
