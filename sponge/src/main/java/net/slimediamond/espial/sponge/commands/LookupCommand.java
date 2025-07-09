package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.common.permission.Permissions;
import net.slimediamond.espial.common.utils.formatting.Format;
import net.slimediamond.espial.sponge.utils.formatting.RecordFormatter;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.List;


public class LookupCommand extends RecordResultCommand {

    public LookupCommand() {
        super(Permissions.LOOKUP, Component.text("Query logs for grief"));

        addAlias("lookup");
        addAlias("l");
    }

    @Override
    public void apply(final CommandContext context, final List<EspialRecord> records) {
        if (records.isEmpty()) {
            context.sendMessage(Format.error("No records found"));
        } else {
            PaginationList.builder()
                    .title(Format.title("Lookup results"))
                    .contents(RecordFormatter.formatRecords(records, false))
                    .padding(Format.PADDING)
                    .sendTo(context.cause().audience());
        }
    }

}
