package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.util.Format;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class BaseCommand extends AbstractCommand {

  public BaseCommand() {
    super(null, Component.text("Root command for Espial"));
    addAlias("espial");
    addAlias("es");

    addChild(new InspectCommand());
    addChild(new InteractiveToggleCommand());
    addChild(new IsThisBlockMineCommand());
    addChild(new NearbySignsCommand());
    addChild(new TransactionCommands.Lookup());
    addChild(new TransactionCommands.Rollback());
    addChild(new TransactionCommands.Restore());
    addChild(new TransactionCommands.RollbackId());
    addChild(new TransactionCommands.RestoreId());
    addChild(new TransactionCommands.Undo());
    addChild(new TransactionCommands.Redo());
    addChild(new TransactionCommands.Near());
    addChild(new WhoPlacedThisCommand());
  }

  @Override
  public CommandResult execute(CommandContext context) throws CommandException {
    context.sendMessage(
        Format.title(
                "Version " + Espial.getInstance().getContainer().metadata().version().toString())
            .append(Component.newline())
            .append(Component.text("Developers: ", Format.THEME_COLOR))
            .append(Component.text("SlimeDiamond", NamedTextColor.GRAY))
            .append(Component.newline())
            .append(Component.text("Use the ", NamedTextColor.GRAY))
            .append(
                Format.commandHint(
                    "help",
                    "/espial help",
                    Component.text("Help command")
                        .appendNewline()
                        .append(Component.text("/espial help"))))
            .append(Component.text(" command for help.", NamedTextColor.GRAY)));

    return CommandResult.success();
  }
}
