package net.slimediamond.espial.sponge.utils;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.sponge.commands.subsystem.Parameters;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.time.Instant;
import java.util.Date;

public class CommandUtils {

    public static ServerPlayer getServerPlayer(@NotNull final CommandContext context) throws CommandException {
        if (!(context.cause().subject() instanceof ServerPlayer player)) {
            throw new CommandException(Component.text("Only players can run this"));
        }
        return player;
    }

    public static EspialQuery.Builder getQueryBuilder(@NotNull final CommandContext context) {
        final EspialQuery.Builder builder = EspialQuery.builder()
                .audience(context.cause().audience());

        context.one(Parameters.AFTER).ifPresent(duration -> {
            final Date date = Date.from(Instant.now().minus(duration));
            builder.after(date);
        });

        context.one(Parameters.BEFORE).ifPresent(duration -> {
            final Date date = Date.from(Instant.now().minus(duration));
            builder.before(date);
        });

        context.all(Parameters.USER).forEach(builder::addUser);
        context.all(Parameters.BLOCK_TYPE).forEach(builder::addBlockType);
        context.all(Parameters.EVENT).forEach(builder::addEvent);

        return builder;
    }

}
