package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.Flags;
import net.slimediamond.espial.sponge.permission.Permissions;
import net.slimediamond.espial.sponge.query.selector.GlobalSelector;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.sql.SQLException;
import java.util.List;

public class PurgeCommand extends RecordResultCommand {

    public PurgeCommand() {
        super(Permissions.PURGE, Component.text("Delete logs within a selection"));

        addAlias("purge");
        addSelector(new GlobalSelector());
        addFlag(Flags.YES, Component.text("Confirm the removal of records"));
    }

    @Override
    public void prerequisites() throws CommandException {
        if (!Espial.getInstance().getConfig().isPurgeCommandEnabled()) {
            throw new CommandException(Format.error("The purge command is currently disabled " +
                    "in the server's config. Please enable it and try again"));
        }
    }

    @Override
    public void apply(final CommandContext context, final List<EspialRecord> records) {
        if (!context.hasFlag(Flags.YES)) {
            context.sendMessage(Format.warn(String.format("This will erase %o records " +
                            "PERMANENTLY. Add --yes to confirm",
                    records.size())));
            return;
        }
        // erase records
        try {
            Espial.getInstance().getDatabase().batchDelete(records.stream().map(EspialRecord::getId).toList());
            context.sendMessage(Format.text(String.format("Erased %o records", records.size())));
        } catch (final SQLException e) {
            context.sendMessage(Format.error("Unable to delete records"));
//            Espial.getInstance().getLogger().error(e);
            e.printStackTrace();
        }
    }

}
