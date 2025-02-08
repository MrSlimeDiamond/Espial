package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.CommandParameters;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class HelpCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext context) {
        ArrayList<Component> contents = new ArrayList<>();

        // Show help for a specific command (if it exists)
        if (context.hasAny(CommandParameters.HELP_COMMAND)) {
            String command = context.requireOne(CommandParameters.HELP_COMMAND);
            Espial.getInstance().getEspialCommand().subcommands().stream().filter(cmd -> cmd.aliases().contains(command)).findFirst().ifPresent(subcommand -> {
                String name = subcommand.aliases().stream().findFirst().get();
                var builder = Component.text()
                        .append(Espial.prefix)
                        .append(Component.text("Help for: ")).append(Component.text(name).color(NamedTextColor.WHITE)
                );

                subcommand.command().shortDescription(context.cause()).ifPresent(desc -> {
                    builder.append(Component.newline())
                            .append(Component.text("Description: ").color(NamedTextColor.YELLOW))
                            .append(desc).color(NamedTextColor.WHITE);
                });

                builder.append(Component.newline())
                       .append(Component.text("Aliases: ").color(NamedTextColor.YELLOW))
                       .append(Component.text(String.join(", ", subcommand.aliases())).color(NamedTextColor.WHITE));

                ArrayList<Component> flags = new ArrayList<>();
                subcommand.command().flags().forEach(flag -> {
                    Collection<String> aliases = flag.aliases();
                    String shortestAlias = aliases.stream().min(Comparator.comparingInt(String::length)).orElse("");

                    AtomicReference<NamedTextColor> flagColour = new AtomicReference<>(NamedTextColor.GREEN);

                    flag.associatedParameter().ifPresent(parameter -> {
                        if (!parameter.isOptional()) {
                            flagColour.set(NamedTextColor.GOLD);
                        }
                    });
                    flags.add(Component.space()
                            .append(Component.text("[").color(NamedTextColor.GRAY))
                            .append(Component.text(shortestAlias).color(flagColour.get())
                                    .hoverEvent(HoverEvent.showText(Component.text(String.join(" | ", aliases)).color(NamedTextColor.WHITE))))
                            .append(Component.text("]").color(NamedTextColor.GRAY))
                    );
                });

                if (!flags.isEmpty()) {
                    builder.append(Component.newline()).append(Component.text("Flags:").color(NamedTextColor.YELLOW));
                    builder.append(flags);
                }

                contents.add(builder.build());
            });
        } else {
            context.sendMessage(Espial.prefix.append(Component.text("Help")));
            Espial.getInstance().getEspialCommand().subcommands().forEach(subcommand -> {
                var builder = Component.text()
                        // little bud, if there are no aliases, idk what to tell you
                        .append(Component.text("/espial " + subcommand.aliases().stream().findFirst().get())
                                .color(NamedTextColor.GREEN)
                                .hoverEvent(HoverEvent.showText(Component.text("Aliases: " + String.join(", ", subcommand.aliases())).append(Component.newline()).append(Component.text("Click for help"))))
                                .clickEvent(ClickEvent.runCommand("/espial help " + subcommand.aliases().stream().findFirst().get()))
                        );

                subcommand.command().shortDescription(context.cause()).ifPresent(desc -> {
                    builder.append(Component.text(" - ")).append(desc);
                });

                contents.add(builder.build());
            });
        }

        for (Component content : contents) {
            context.sendMessage(content);
        }

        return CommandResult.success();
    }
}
