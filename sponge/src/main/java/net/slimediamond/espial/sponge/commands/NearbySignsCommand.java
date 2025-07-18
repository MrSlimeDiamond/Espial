package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.record.EspialSignModifyRecord;
import net.slimediamond.espial.common.permission.Permissions;
import net.slimediamond.espial.sponge.query.selector.DefaultRangeSelector;
import org.spongepowered.api.command.parameter.CommandContext;

import java.util.Comparator;
import java.util.List;

public class NearbySignsCommand extends RecordResultCommand {

    public NearbySignsCommand() {
        super(Permissions.NEARBY_SIGNS, Component.text("Lookup signs near you"));

        addAlias("nearbysigns");

        addPredicate(record -> record instanceof EspialBlockRecord
                || record instanceof EspialSignModifyRecord);
        addPredicate(record -> record.getTarget().contains("sign"));
        addSelector(new DefaultRangeSelector());
    }

    @Override
    public void apply(final CommandContext context, final List<EspialRecord> records) {
        records.sort(Comparator.comparingInt(EspialRecord::getId).reversed());
        displayRecords(context, records, true);
    }

}
