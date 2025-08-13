package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.sponge.permission.Permissions;

public class RestoreCommand extends TransactionCommand {

    public RestoreCommand(final boolean preview) {
        super(TransactionTypes.RESTORE.get(), Permissions.RESTORE, Component.text("Restore a selection"), preview);

        addAlias("restore");
        addAlias("rs");

        addPredicate(EspialRecord::isRolledBack);
    }

}
