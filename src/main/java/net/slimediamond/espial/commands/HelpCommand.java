package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class HelpCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        // TODO: automation
        context.sendMessage(Component.text()
                .content("Espial | Help")
                .color(NamedTextColor.GREEN)
                .append(Component.newline())
                .append(Component.text("/espial help", NamedTextColor.YELLOW))
                .append(Component.text(" - ", NamedTextColor.WHITE))
                .append(Component.text("Shows this menu", NamedTextColor.GREEN))
                .append(Component.newline())
                .append(Component.text("/espial info", NamedTextColor.YELLOW).clickEvent(ClickEvent.runCommand("/espial info")))
                .append(Component.text(" - ", NamedTextColor.WHITE))
                .append(Component.text("Show info about the plugin", NamedTextColor.GREEN))
                .append(Component.newline())
                .append(Component.text("/espial lookup [worldedit] [<x> <y> <z>] [<x2> <y2> <z2>]").color(NamedTextColor.YELLOW).clickEvent(ClickEvent.runCommand("/espial lookup")))
                .append(Component.text(" - ").color(NamedTextColor.WHITE))
                .append(Component.text("Lookup specific coords or a region"))
                .append(Component.newline())
                .append(Component.text("/espial inspect <id>").color(NamedTextColor.YELLOW).clickEvent(ClickEvent.runCommand("/espial inspect")))
                .append(Component.text(" - ").color(NamedTextColor.WHITE))
                .append(Component.text("Inspect a specific block ID"))
                .build()
        );

        return CommandResult.success();
    }
}
