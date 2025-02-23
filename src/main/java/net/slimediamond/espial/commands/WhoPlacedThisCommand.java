package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.util.Format;
import net.slimediamond.espial.util.RayTraceUtil;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.LocatableBlock;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class WhoPlacedThisCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandContext context)
            throws CommandException {
        if (context.cause().root() instanceof Player player) {

            Optional<LocatableBlock> result =
                    RayTraceUtil.getBlockFacingPlayer(player);
            if (result.isPresent()) {
                LocatableBlock block = result.get();

                try {
                    Espial.getInstance().getDatabase()
                            .getBlockOwner(block.location().blockX(),
                                    block.location().blockY(),
                                    block.location().blockZ())
                            .ifPresentOrElse(user -> {
                                context.sendMessage(Format.component(Component.text()
                                        .append(Component.text(user.name())
                                                .color(NamedTextColor.YELLOW)
                                                .append(Component.space())
                                                .append(Component.text(
                                                                "placed this ")
                                                        .color(NamedTextColor.WHITE))
                                                .append(Component.text(
                                                                block.blockState()
                                                                        .type()
                                                                        .key(RegistryTypes.BLOCK_TYPE)
                                                                        .formatted()
                                                                        .split(":")[1])
                                                        .color(NamedTextColor.YELLOW))
                                                .append(Component.text(".")
                                                        .color(NamedTextColor.WHITE))
                                        )));
                            }, () -> {
                                context.sendMessage(Format.error("Could not " +
                                        "find a block owner which was a " +
                                        "player."));
                            });
                } catch (SQLException | ExecutionException |
                         InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                context.sendMessage(Format.noBlockFound());
            }
        } else {
            context.sendMessage(Format.playersOnly());
        }

        return CommandResult.success();
    }
}
