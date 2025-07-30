package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.api.transaction.Transaction;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.sponge.permission.Permissions;
import net.slimediamond.espial.sponge.utils.CommandUtils;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class RedoCommand extends AbstractCommand {

    public RedoCommand() {
        super(Permissions.UNDO, Component.text("Redo what you just undid"));

        addAlias("redo");
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException {
        final ServerPlayer player = CommandUtils.getServerPlayer(context);
        final Optional<Transaction> transactionOptional = Espial.getInstance().getEspialService()
                .getTransactionManager()
                .redo(player.uniqueId());
        if (transactionOptional.isPresent()) {
            context.sendMessage(Format.text("Redid " + transactionOptional.get().getRecords().size() + " undone reversal(s)"));
        } else {
            context.sendMessage(Format.error("Nothing to redo"));
        }
        return CommandResult.success();
    }

}
