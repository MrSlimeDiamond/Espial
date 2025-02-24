package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.util.Format;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class BaseCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext context)
            throws CommandException {
        context.sendMessage(
                Format.title(
                        "Version "
                         + Espial.getInstance().getContainer().metadata().version().toString())
                .append(Component.newline())
                .append(Component.text("Developers: ", Format.THEME_COLOR))
                .append(Component.text("SlimeDiamond", Format.TEXT_COLOR))
                .append(Component.newline())
                .append(Component.text("Use ", Format.THEME_COLOR))
                .append(Component.text()
                        .content("/espial help")
                        .clickEvent(ClickEvent.runCommand("/espial help"))
                        .color(Format.TEXT_COLOR)
                )
                .append(Component.text(" for help.", Format.THEME_COLOR))

        );
        return CommandResult.success();
    }
}
