package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.sponge.permission.Permissions;
import net.slimediamond.espial.sponge.commands.subsystem.Flags;
import org.spongepowered.api.command.parameter.CommandContext;

import java.util.Comparator;
import java.util.List;


public class LookupCommand extends RecordResultCommand {

    public LookupCommand() {
        super(Permissions.LOOKUP, Component.text("Query logs for grief"));

        addAlias("lookup");
        addAlias("l");
        addFlags(Flags.SPREAD_FLAG);
    }

    @Override
    public void apply(final CommandContext context, final List<EspialRecord> records) {
        records.sort(Comparator.comparingInt(EspialRecord::getId).reversed());
        displayRecords(context, records, context.hasFlag("s"));
    }

}
