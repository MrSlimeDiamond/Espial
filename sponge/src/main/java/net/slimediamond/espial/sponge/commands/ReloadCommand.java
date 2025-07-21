package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.sponge.permission.Permissions;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class ReloadCommand extends AbstractCommand {

    public ReloadCommand() {
        super(Permissions.RELOAD, Component.text("Reload the plugin and apply changes from the config"));

        addAlias("reload");
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException {
        try {
            Espial.getInstance().reload();
            context.sendMessage(Format.text("Reloaded!"));
            return CommandResult.success();
        } catch (final Throwable t) {
            Espial.getInstance().getLogger().error("Unable to reload", t);
            return CommandResult.error(Format.error("Unable to reload, check the console"));
        }
    }

}
