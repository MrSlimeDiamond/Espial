package net.slimediamond.espial.commands;

import net.slimediamond.espial.Espial;
import net.slimediamond.espial.util.Format;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

import java.util.List;
import java.util.UUID;

public class InteractiveToggleCommand implements CommandExecutor {
  @Override
  public CommandResult execute(CommandContext context) throws CommandException {
    if (context.cause().root() instanceof Player player) {
      List<UUID> inspectingPlayers = Espial.getInstance().getInspectingPlayers();
      if (inspectingPlayers.contains(player.profile().uuid())) {
        // turn it off
        inspectingPlayers.remove(player.profile().uuid());
        context.sendMessage(Format.text("Interactive mode disabled."));
      } else {
        inspectingPlayers.add(player.profile().uuid());
        context.sendMessage(Format.text("Interactive mode enabled."));
      }
    } else {
      context.sendMessage(Format.playersOnly());
    }

    return CommandResult.success();
  }
}
