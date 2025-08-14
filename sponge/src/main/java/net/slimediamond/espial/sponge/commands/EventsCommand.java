package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.sponge.permission.Permissions;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;

public class EventsCommand extends AbstractCommand {

    public EventsCommand() {
        super(Permissions.EVENTS, Component.text("List events which are listened for"));

        addAlias("events");
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException {
        PaginationList.builder()
                .title(Format.title("Events"))
                .padding(Format.PADDING)
                .contents(EspialEvents.registry().streamEntries().map(entry ->
                                Format.accent("- ")
                                        .append(entry.value().asComponent().color(
                                                Espial.getInstance().getConfig().getIgnoredEvents().contains(entry.key())
                                                        ? NamedTextColor.RED
                                                        : Format.THEME_COLOR)))
                        .toList())
                .sendTo(context.cause().audience());
        return CommandResult.success();
    }

}
