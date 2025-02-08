package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Database;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.util.RayTraceUtil;
import org.checkerframework.checker.units.qual.N;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.LocatableBlock;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class WhoPlacedThisCommand implements CommandExecutor {
    private Database database;

    public WhoPlacedThisCommand(Database database) {
        this.database = database;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        if (context.cause().root() instanceof Player player) {

            Optional<LocatableBlock> result = RayTraceUtil.getBlockFacingPlayer(player);
            if (result.isPresent()) {
                LocatableBlock block = result.get();

                try {
                    database.getBlockOwner(block.location().blockX(), block.location().blockY(), block.location().blockZ()).ifPresentOrElse(user -> {
                        context.sendMessage(Espial.prefix
                                    .append(Component.text(user.name()).color(NamedTextColor.YELLOW)
                                    .append(Component.space())
                                    .append(Component.text("placed this ").color(NamedTextColor.WHITE))
                                    .append(Component.text(block.blockState().type().key(RegistryTypes.BLOCK_TYPE).formatted().split(":")[1]).color(NamedTextColor.YELLOW))
                                    .append(Component.text(".").color(NamedTextColor.WHITE))
                                ));
                    }, () -> {
                        context.sendMessage(Espial.prefix.append(Component.text("Could not determine a player block owner.").color(NamedTextColor.RED)));
                    });
                } catch (SQLException | ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                context.sendMessage(Espial.prefix.append(Component.text("Could not detect a block. Move closer, perhaps?").color(NamedTextColor.RED)));
            }
        } else {
            context.sendMessage(Component.text("This command can currently only be executed by players.").color(NamedTextColor.RED));
        }

        return CommandResult.success();
    }
}
