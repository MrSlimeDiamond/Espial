package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.transaction.TransactionTypes;
import net.slimediamond.espial.sponge.permission.Permissions;

public class RollbackCommand extends TransactionCommand {

    public RollbackCommand(final boolean preview) {
        super(TransactionTypes.ROLLBACK.get(), Permissions.ROLLBACK, Component.text("Rollback a selection"), preview);

        addAlias("rollback");
        addAlias("rb");

        addPredicate(record -> !record.isRolledBack());
    }

}
