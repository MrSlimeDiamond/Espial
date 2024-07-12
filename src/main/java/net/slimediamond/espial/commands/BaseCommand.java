package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class BaseCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        context.sendMessage(Component.text()
            .content("Espial - 1.0-SNAPSHOT")
            .color(NamedTextColor.GREEN)
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
