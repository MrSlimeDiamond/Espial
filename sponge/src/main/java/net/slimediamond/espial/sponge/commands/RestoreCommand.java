package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.common.permission.Permissions;

public class RestoreCommand extends TransactionCommand {

    public RestoreCommand() {
        super(TransactionTypes.RESTORE.get(), Permissions.RESTORE, Component.text("Restore a selection"));

        addAlias("restore");
        addAlias("rs");

        addPredicate(EspialRecord::isRolledBack);
    }

}
