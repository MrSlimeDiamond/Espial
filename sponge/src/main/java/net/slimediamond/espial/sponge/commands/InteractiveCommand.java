package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.sponge.permission.Permissions;
import net.slimediamond.espial.sponge.utils.CommandUtils;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.List;
import java.util.UUID;

public class InteractiveCommand extends AbstractCommand {

    public InteractiveCommand() {
        super(Permissions.INTERACTIVE, Component.text("Enter an interactive inspector"));

        addAlias("interactive");
        addAlias("i");
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException {
        final ServerPlayer player = CommandUtils.getServerPlayer(context);

        List<UUID> inspectingUsers = Espial.getInstance().getEspialService().getInspectingUsers();

        if (inspectingUsers.contains(player.uniqueId())) {
            inspectingUsers.remove(player.uniqueId());
            context.sendMessage(Format.text("Interactive mode disabled"));
        } else {
            inspectingUsers.add(player.uniqueId());
            context.sendMessage(Format.text("Interactive mode enabled"));
        }
        return CommandResult.success();
    }

}
