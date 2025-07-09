package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialBlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.common.permission.Permissions;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.registry.RegistryTypes;

import java.util.Comparator;
import java.util.List;

public class NearbySignsCommand extends RecordResultCommand {

    public NearbySignsCommand() {
        super(Permissions.NEARBY_SIGNS, Component.text("Lookup signs near you"));

        addAlias("nearbysigns");
        addPredicate(record -> record instanceof EspialBlockRecord);
        addPredicate(record -> ((EspialBlockRecord)record).getBlockState()
                .type().key(RegistryTypes.BLOCK_TYPE).formatted().contains("sign"));
    }

    @Override
    public void apply(final CommandContext context, final List<EspialRecord> records) {
        records.sort(Comparator.comparingInt(EspialRecord::getId).reversed());
        displayRecords(context, records, true);
    }

}
