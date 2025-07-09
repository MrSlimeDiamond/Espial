package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.common.permission.Permissions;
import org.spongepowered.api.command.parameter.CommandContext;

import java.util.List;


public class LookupCommand extends RecordResultCommand {

    public LookupCommand() {
        super(Permissions.LOOKUP, Component.text("Query logs for grief"));

        addAlias("lookup");
        addAlias("l");
    }

    @Override
    public void apply(final CommandContext context, final List<EspialRecord> records) {
        displayRecords(context, records, false);
    }

}
