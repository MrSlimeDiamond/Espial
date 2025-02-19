package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.UUID;

public class InteractiveToggleCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        if (context.cause().root() instanceof Player player) {
            ArrayList<UUID> inspectingPlayers = Espial.getInstance().getInspectingPlayers();
            if (inspectingPlayers.contains(player.profile().uuid())) {
                // turn it off
                inspectingPlayers.remove(player.profile().uuid());
                context.sendMessage(Espial.prefix.append(Component.text("Interactive mode disabled.").color(NamedTextColor.WHITE)));
            } else {
                inspectingPlayers.add(player.profile().uuid());
                context.sendMessage(Espial.prefix.append(Component.text("Interactive mode enabled.").color(NamedTextColor.WHITE)));
            }
        } else {
            context.sendMessage(Component.text("You must be a player to run this!").color(NamedTextColor.RED));
        }

        return CommandResult.success();
    }
}
