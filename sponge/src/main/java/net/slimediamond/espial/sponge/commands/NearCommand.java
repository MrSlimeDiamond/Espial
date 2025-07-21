package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.sponge.permission.Permissions;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.sponge.commands.subsystem.Flags;
import net.slimediamond.espial.sponge.utils.formatting.RecordFormatter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerLocation;

// TODO: parameters
public class NearCommand extends AbstractCommand {

    public NearCommand() {
        super(Permissions.LOOKUP, Component.text("Look up blocks near you"));

        addAlias("near");
        addFlags(Flags.SPREAD_FLAG);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException {
        final ServerLocation location = context.cause().location()
                .orElseThrow(() -> new CommandException(Component.text("You need a location to use this")));

        final int range = Espial.getInstance().getConfig().getNearRange();
        context.sendMessage(Format.defaults("Range: " + range + " blocks"));

        Espial.getInstance().getEspialService().query(EspialQuery.builder()
                .worldKey(location.worldKey())
                .minimum(location.blockPosition().sub(range, range, range))
                .maximum(location.blockPosition().add(range, range, range))
                .audience(context.cause().audience())
                .build())
                .thenAccept(records -> {
                    if (records.isEmpty()) {
                        context.sendMessage(Format.NO_RECORDS_FOUND);
                    } else {
                        PaginationList.builder()
                                .title(Format.title("Nearby results"))
                                .contents(RecordFormatter.formatRecords(records, context.hasFlag("s")))
                                .padding(Format.PADDING)
                                .sendTo(context.cause().audience());
                    }
                });
        return CommandResult.success();
    }

}
