package net.slimediamond.espial.sponge.commands.subsystem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.slimediamond.espial.common.utils.formatting.Format;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.Comparator;

public class HelpCommand extends AbstractCommand {

    private final AbstractCommand parent;

    public HelpCommand(AbstractCommand parent) {
        super(parent.getPermission(), Component.text("Help subcommand"));

        addAlias("help");
        addAlias("?");
        showInHelp(false);

        this.parent = parent;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        final TextComponent.Builder header = Component.text()
                .append(Component.text("Description: ").color(Format.TITLE_COLOR))
                .append(parent.getDescription().color(Format.TEXT_COLOR));

        if (!parent.getFlags().isEmpty()) {
            header.appendNewline().append(Component.text("Flags:").color(Format.TITLE_COLOR));

            parent.getFlags().forEach((flag, description) -> {
                final boolean optional = flag.associatedParameter().map(Parameter::isOptional).orElse(false);

                final TextColor color = optional ? NamedTextColor.GOLD : NamedTextColor.AQUA;

                header.append(Format.dull(" ("))
                        .append(Component.text(flag.aliases().stream().min(Comparator.comparingInt(String::length)).orElseThrow())
                                .color(color)
                                .hoverEvent(HoverEvent.showText(Component.text()
                                        .append(Component.text(optional ? "Flag" : "Flag - requires value").color(color))
                                        .appendNewline()
                                        .append(description))))
                        .append(Format.dull(")"));
            });
        }

        final PaginationList.Builder builder = PaginationList.builder()
                .title(Format.title("Help")
                        .append(Format.dull(" -=- "))
                        .append(Format.dull("("))
                        .append(Format.accent(String.join(", ", this.parent.getAliases())))
                        .append(Format.dull(")")))
                .padding(Format.PADDING)
                .header(header.build())
                .contents(this.parent.getChildren().stream()
                        .filter(child -> child.showInHelp)
                        .filter(child -> context.hasPermission(child.getPermission().get()))
                        .map(child ->
                            Component.text()
                                    .append(Format.accent("> "))
                                    .append(Component.text(child.getAliases().getFirst()).color(Format.THEME_COLOR)
                                            .hoverEvent(HoverEvent.showText(Format.detail("Aliases",
                                                    String.join(", ", child.getAliases())))))
                                    .appendSpace()
                                    .append(child.getDescription().color(Format.TEXT_COLOR))
                                    .clickEvent(ClickEvent.runCommand("/espial " + child.getAliases().getFirst() + " help"))
                                    .build().asComponent())
                            .toList());

        builder.sendTo(context.cause().audience());
        return CommandResult.success();
    }

}
