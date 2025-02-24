package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.CommandParameters;
import net.slimediamond.espial.Commands;
import net.slimediamond.espial.util.Format;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class HelpCommand implements CommandExecutor {
  @Override
  public CommandResult execute(CommandContext context) {
    List<Component> contents = new ArrayList<>();

    // Show help for a specific command (if it exists)
    if (context.hasAny(CommandParameters.HELP_COMMAND)) {
      String command = context.requireOne(CommandParameters.HELP_COMMAND);
      // FIXME: Stuff like /help lookup will work for /es lookup, but something else like /help
      // whoplacedthis doesn't
      Commands.commands.get(0).subcommands().stream()
          .filter(cmd -> cmd.aliases().contains(command))
          .findFirst()
          .ifPresent(
              subcommand -> {
                String name = subcommand.aliases().stream().findFirst().get();

                TextComponent.Builder builder =
                    Component.text().append(Format.text("Help for: " + name));

                subcommand
                    .command()
                    .shortDescription(context.cause())
                    .ifPresent(
                        desc -> {
                          builder
                              .append(Component.newline())
                              .append(Component.text("Description: ").color(NamedTextColor.YELLOW))
                              .append(desc)
                              .color(NamedTextColor.WHITE);
                        });

                builder
                    .append(Component.newline())
                    .append(Component.text("Aliases: ").color(NamedTextColor.YELLOW))
                    .append(
                        Component.text(String.join(", ", subcommand.aliases()))
                            .color(NamedTextColor.WHITE));

                List<Component> flags = new ArrayList<>();
                subcommand
                    .command()
                    .flags()
                    .forEach(
                        flag -> {
                          Collection<String> aliases = flag.aliases();
                          String shortestAlias =
                              aliases.stream()
                                  .min(Comparator.comparingInt(String::length))
                                  .orElse("");

                          AtomicReference<NamedTextColor> flagColour =
                              new AtomicReference<>(NamedTextColor.GREEN);

                          flag.associatedParameter()
                              .ifPresent(
                                  parameter -> {
                                    if (!parameter.isOptional()) {
                                      flagColour.set(NamedTextColor.GOLD);
                                    }
                                  });
                          flags.add(
                              Component.space()
                                  .append(Component.text("[").color(NamedTextColor.GRAY))
                                  .append(
                                      Component.text(shortestAlias)
                                          .color(flagColour.get())
                                          .hoverEvent(
                                              HoverEvent.showText(
                                                  Component.text(String.join(" | ", aliases))
                                                      .color(NamedTextColor.WHITE))))
                                  .append(Component.text("]").color(NamedTextColor.GRAY)));
                        });

                if (!flags.isEmpty()) {
                  builder
                      .append(Component.newline())
                      .append(Component.text("Flags:").color(NamedTextColor.YELLOW));
                  builder.append(flags);
                }

                contents.add(builder.build());
              });
    } else {
      // FIXME: Other root commands (like /whoplacedthis)
      context.sendMessage(Format.text("Help"));
      Command.Parameterized command = Commands.commands.get(0);
      List<Parameter.Subcommand> sortedSubcommands =
          command.subcommands().stream()
              .sorted(
                  Comparator.comparing(
                      subcommand ->
                          subcommand.aliases().stream()
                              .max(Comparator.comparingInt(String::length))
                              .get()))
              .toList();

      for (Parameter.Subcommand subcommand : sortedSubcommands) {
        if (subcommand.command().executionRequirements().test(context.cause())) {
          var builder =
              Component.text()
                  .append(
                      Component.text(
                              "/espial "
                                  + subcommand.aliases().stream()
                                      .max(Comparator.comparingInt(String::length))
                                      .get())
                          .color(NamedTextColor.GREEN)
                          .hoverEvent(
                              HoverEvent.showText(
                                  Component.text(
                                          "Aliases: " + String.join(", ", subcommand.aliases()))
                                      .append(Component.newline())
                                      .append(Component.text("Click for help"))))
                          .clickEvent(
                              ClickEvent.runCommand(
                                  "/espial help "
                                      + subcommand.aliases().stream()
                                          .max(Comparator.comparingInt(String::length))
                                          .get())));

          Optional<Component> description = subcommand.command().shortDescription(context.cause());

          if (description.isPresent()) {
            builder.append(Component.text(" - ")).append(description.get());
          } else {
            continue;
          }

          contents.add(builder.build());
        }
      }
    }

    for (Component content : contents) {
      context.sendMessage(content);
    }

    return CommandResult.success();
  }
}
