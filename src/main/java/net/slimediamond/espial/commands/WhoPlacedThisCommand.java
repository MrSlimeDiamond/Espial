package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.util.Format;
import net.slimediamond.espial.util.RayTraceUtil;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.LocatableBlock;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class WhoPlacedThisCommand extends AbstractCommand {

    public WhoPlacedThisCommand() {
        super("espial.command.whoplacedthis", Component.text("Check who placed a " +
                "block and nothing else"));
        addAlias("whoplacedthis");
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        if (context.cause().root() instanceof Player player) {

            Optional<LocatableBlock> result = RayTraceUtil.getBlockFacingPlayer(player);
            if (result.isPresent()) {
                LocatableBlock block = result.get();

                try {
                    Espial.getInstance().getDatabase().getBlockOwner(block.serverLocation().worldKey().formatted(),
                                    block.location().blockX(),
                                    block.location().blockY(),
                                    block.location().blockZ())
                            .ifPresentOrElse(user -> context.sendMessage(Format.component(
                                    Component.text("This ")
                                            .append(block
                                                    .blockState()
                                                    .type()
                                                    .asComponent()
                                                    .color(Format.TEXT_COLOR)
                                                    .append(Component.space())
                                                    .append(Component.text("was placed by ")
                                                            .color(Format.THEME_COLOR))
                                                    .append(Component.text(user.name()).color(Format.TEXT_COLOR))
                                                    .append(Component.text(".").color(Format.THEME_COLOR))))), () -> context.sendMessage(
                                    Format.error("Could not find a block owner which was a player.")));
                } catch (SQLException | ExecutionException | InterruptedException e) {
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
