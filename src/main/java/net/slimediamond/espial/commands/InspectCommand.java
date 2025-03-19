package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.commands.subsystem.AbstractCommand;
import net.slimediamond.espial.commands.subsystem.CommandParameters;
import net.slimediamond.espial.util.Format;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.math.vector.Vector3d;

import java.util.concurrent.TimeUnit;

public class InspectCommand extends AbstractCommand {

    /**
     * Constructor for a command
     */
    InspectCommand() {
        super("espial.command.inspect", Component.text("Inspect command callback " +
                "for /espial lookup"));
        addAlias("inspect");
        addChild(new StopCommand());
        addParameter(CommandParameters.GENERIC_ID);
        showInHelp(false);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Player player = (Player) context.cause().root();

        if (player == null) {
            context.sendMessage(Format.playersOnly());
            return CommandResult.success();
        }

        if (!context.hasAny(CommandParameters.GENERIC_ID)) {
            this.stopOutline(player);
            return CommandResult.success();
        }

        int id = context.requireOne(CommandParameters.GENERIC_ID);

        try {
            EspialRecord record = Espial.getInstance().getDatabase().queryId(id);

            if (record == null) {
                context.sendMessage(Format.error("Could not find a database index with that ID."));
            }

            this.teleportPlayer(player, record.getAction());

            Component displayName = Format.getDisplayName(record.getAction());
            String undoActionMessage;
            String undoCommand;
            if (record.isRolledBack()) {
                undoActionMessage = "RESTORE";
                undoCommand = "/espial restoreid " + record.getId();
            } else {
                undoActionMessage = "ROLLBACK";
                undoCommand = "/espial rollbackid " + record.getId();
            }

            PaginationList.builder()
                    .title(Format.title("Looking closer at an action..."))
                    .padding(Format.PADDING)
                    .contents(Component.text()
                            .append(Format.chip("STOP", NamedTextColor.RED)
                                    .clickEvent(ClickEvent.runCommand("/espial inspect stop"))
                                    .hoverEvent(HoverEvent.showText(Component.text("Stop particles"))))
                            .append(Format.chip("TP", NamedTextColor.GREEN)
                                    .clickEvent(
                                            SpongeComponents.executeCallback(cause -> this.teleportPlayer(player, record.getAction())))
                                    .hoverEvent(
                                            HoverEvent.showText(Component.text("Teleport to this block"))))
                            .append(Format.chip(undoActionMessage, NamedTextColor.YELLOW)
                                    .clickEvent(ClickEvent.runCommand(undoCommand))
                                    .hoverEvent(HoverEvent.showText(Component.text("Undo this action"))))
                            .append(Component.newline())
                            .append(Component.text("Source: ").color(Format.THEME_COLOR))
                            .append(displayName.color(Format.NAME_COLOR))
                            .append(Component.newline())
                            .append(Component.text("Type: ").color(Format.THEME_COLOR))
                            .append(Format.makeHoverableAction(record.getAction().getEventType(), false)
                                    .color(Format.INFO_COLOR))
                            .append(Component.newline())
                            .append(Component.text("Coordinates: ").color(Format.THEME_COLOR))
                            .append(Component.text(record.getAction().getX()
                                            + " "
                                            + record.getAction().getY()
                                            + " "
                                            + record.getAction().getZ())
                                    .color(Format.INFO_COLOR))
                            .build())
                    .sendTo(context.cause().audience());

            // We should cancel an existing particle effect if there is one
            this.stopOutline(player);

            if (record.getAction() instanceof BlockAction action) {
                this.startOutline(player, action);
            }

            return CommandResult.success();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void startOutline(Player player, BlockAction action) {
        ParticleEffect particleEffect = ParticleEffect.builder().type(ParticleTypes.FLAME).build();

        double[][] offsets = {
                {0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {1, 1, 0},
                {0, 0, 1}, {1, 0, 1}, {0, 1, 1}, {1, 1, 1},
                {0.5, 0, 0}, {0.5, 1, 0}, {0.5, 0, 1}, {0.5, 1, 1},
                {0, 0.5, 0}, {1, 0.5, 0}, {0, 0.5, 1}, {1, 0.5, 1},
                {0, 0, 0.5}, {1, 0, 0.5}, {0, 1, 0.5}, {1, 1, 0.5}
        };

        Task task = Task.builder()
                .execute(() -> {
                    for (double[] offset : offsets) {
                        player.spawnParticles(
                                particleEffect,
                                action.getServerLocation().position().add(offset[0], offset[1], offset[2]));
                    }
                })
                .interval(1, TimeUnit.SECONDS)
                .plugin(Espial.getInstance().getContainer())
                .build();

        ScheduledTask scheduledTask = Sponge.game().asyncScheduler().submit(task);
        Espial.getInstance().getBlockOutlines().put(player, scheduledTask);
    }

    private void stopOutline(Player player) {
        if (Espial.getInstance().getBlockOutlines().containsKey(player)) {
            Espial.getInstance().getBlockOutlines().get(player).cancel();
            Espial.getInstance().getBlockOutlines().remove(player);
        }
        player.sendMessage(Format.text("Inspection particles stopped."));
    }

    private void teleportPlayer(Player player, Action action) {
        player.setPosition(new Vector3d(action.getX(), action.getY(), action.getZ()));
    }

    private class StopCommand extends AbstractCommand {
        StopCommand() {
            super("espial.command.inspect", Component.text("Stop inspecting"));
            addAlias("stop");
        }

        @Override
        public CommandResult execute(CommandContext context) {
            if (context.subject() instanceof Player player) {
                stopOutline(player);
            } else {
                context.sendMessage(Format.playersOnly());
            }
            return CommandResult.success();
        }
    }
}
