package net.slimediamond.espial.sponge.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.sponge.commands.subsystem.RootOnlyCommand;
import net.slimediamond.espial.sponge.permission.Permissions;
import net.slimediamond.espial.sponge.utils.CommandUtils;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scheduler.Task;

public class PreviewCommand extends RootOnlyCommand {

    public PreviewCommand() {
        super(Permissions.PREVIEW, Component.text("Preview changes that a transaction will make"));

        addAlias("preview");
        addAlias("p");
        addChild(new ApplyCommand());
        addChild(new CancelCommand());
        addChild(new RollbackCommand(true));
        addChild(new RestoreCommand(true));
    }

    public static class ApplyCommand extends AbstractCommand {

        public ApplyCommand() {
            super(Permissions.PREVIEW, Component.text("Apply your previewed changes"));

            addAlias("apply");
            addAlias("a");
        }

        @Override
        public CommandResult execute(final CommandContext context) throws CommandException {
            final ServerPlayer player = CommandUtils.getServerPlayer(context);
            if (Espial.getInstance().getEspialService().getPreviewManager().apply(player.uniqueId())) {
                context.sendMessage(Format.text("Preview applied"));
                return CommandResult.success();
            } else {
                return CommandResult.error(Format.error("No preview is available for you to apply"));
            }
        }

    }

    public static class CancelCommand extends AbstractCommand {

        public CancelCommand() {
            super(Permissions.PREVIEW, Component.text("Cancel your preview"));

            addAlias("cancel");
            addAlias("c");
        }

        @Override
        public CommandResult execute(final CommandContext context) throws CommandException {
            final ServerPlayer player = CommandUtils.getServerPlayer(context);
            Sponge.asyncScheduler().submit(Task.builder()
                    .execute(() -> {
                        if (Espial.getInstance().getEspialService().getPreviewManager().cancel(player)) {
                            context.sendMessage(Format.text("Current preview cancelled"));
                        } else {
                            context.sendMessage(Format.error("No preview is available for you to cancel"));
                        }
                    })
                    .plugin(Espial.getInstance().getContainer())
                    .build());
            return CommandResult.success();
        }

    }

}
