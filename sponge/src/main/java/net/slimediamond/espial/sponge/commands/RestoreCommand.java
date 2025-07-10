package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.common.permission.Permissions;
import net.slimediamond.espial.common.utils.formatting.Format;

import java.util.Comparator;
import java.util.List;

public class RestoreCommand extends TransactionCommand {

    public RestoreCommand() {
        super(TransactionTypes.RESTORE.get(), Permissions.RESTORE, Component.text("Restore a selection"));

        addAlias("restore");
        addAlias("rs");

        //addPredicate(EspialRecord::isRolledBack);
    }

    @Override
    public void sort(final List<EspialRecord> records) {
        records.sort(Comparator.comparingInt(EspialRecord::getId));
    }

    @Override
    public Component showResults(final List<EspialRecord> records) {
        return Format.text("Restored " + records.size() + " record(s)");
    }

}
