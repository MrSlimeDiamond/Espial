package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class BaseCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        context.sendMessage(Component.text()
            .content("Espial")
            .color(NamedTextColor.GREEN)
            .append(Component.text(" - ").color(NamedTextColor.WHITE))
            .append(Component.text("Version ").color(NamedTextColor.GREEN))
            .append(Component.text(Espial.getInstance().getContainer().metadata().version().toString()).color(NamedTextColor.WHITE))
            .append(Component.newline())
            .append(Component.text("Developers: ", NamedTextColor.GREEN))
            .append(Component.text("SlimeDiamond", NamedTextColor.YELLOW))
            .append(Component.newline())
            .append(Component.text("Use ", NamedTextColor.GREEN))
            .append(Component.text()
                    .content("/espial help")
                    .clickEvent(ClickEvent.runCommand("/espial help"))
                    .color(NamedTextColor.YELLOW)
            )
            .append(Component.text(" for help.", NamedTextColor.GREEN))
            .build()
        );

        return CommandResult.success();
    }
}
