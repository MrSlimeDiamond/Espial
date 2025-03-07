package net.slimediamond.espial.commands;

import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.util.Format;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

public class InteractiveToggleCommand extends AbstractCommand {

  /**
   * Constructor for a command
   */
  InteractiveToggleCommand() {
    super("espial.command.interactive", Component.text("Enable an " +
            "interactive inspector"));
    addAlias("interactive");
    addAlias("i");
  }

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
