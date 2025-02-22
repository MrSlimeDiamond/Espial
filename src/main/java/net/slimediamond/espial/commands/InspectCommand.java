package net.slimediamond.espial.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.CommandParameters;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.Action;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.util.MessageUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandExecutor;
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

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class InspectCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Player player = (Player) context.cause().root();

        if (player == null) {
            context.sendMessage(Component.text("Only players can run this!").color(NamedTextColor.RED));
            return CommandResult.success();
        }

        if (!context.hasAny(CommandParameters.GENERIC_ID)) {
            this.stopOutline(player);
            context.sendMessage(Component.text("No longer inspecting.").color(NamedTextColor.GREEN));
            return CommandResult.success();
        }

        int id = context.requireOne(CommandParameters.GENERIC_ID);

        try {
            EspialRecord record = Espial.getInstance().getDatabase().queryId(id);

            if (record == null) {
                return CommandResult.error(Component.text("Unable to find a database index with that ID!"));
            }

            this.teleportPlayer(player, record.getAction());

            Component displayName = MessageUtil.getDisplayName(record.getAction());
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
                    .title(Espial.prefix.append(Component.text("Looking closer at an action...").color(NamedTextColor.WHITE)))
                    .contents(Component.text()
                        .append(Component.text("[")
                                .color(NamedTextColor.GRAY)
                                .append(Component.text("STOP").color(NamedTextColor.RED))
                                .append(Component.text("]").color(NamedTextColor.GRAY))
                                .clickEvent(ClickEvent.runCommand("/espial inspect stop"))
                                .hoverEvent(HoverEvent.showText(Component.text("Stop particles"))))
                        .append(Component.text(" [")
                                .color(NamedTextColor.GRAY)
                                .append(Component.text("TP").color(NamedTextColor.GREEN))
                                .append(Component.text("]").color(NamedTextColor.GRAY))
                                .clickEvent(SpongeComponents.executeCallback(cause -> this.teleportPlayer(player, record.getAction())))
                                .hoverEvent(HoverEvent.showText(Component.text("Teleport to this block"))))
                        .append(Component.text(" [")
                                .color(NamedTextColor.GRAY)
                                .append(Component.text(undoActionMessage).color(NamedTextColor.YELLOW))
                                .append(Component.text("]").color(NamedTextColor.GRAY))
                                .clickEvent(ClickEvent.runCommand(undoCommand))
                                .hoverEvent(HoverEvent.showText(Component.text("Undo this action"))))
                        .append(Component.newline())
                        .append(Component.text("Source: ").color(NamedTextColor.GREEN))
                        .append(displayName)
                        .append(Component.newline())
                        .append(Component.text("Type: ").color(NamedTextColor.GREEN))
                        .append(MessageUtil.makeHoverableAction(record.getAction().getEventType(), false).color(NamedTextColor.YELLOW))
                        .append(Component.newline())
                        .append(Component.text("Coordinates: ").color(NamedTextColor.GREEN))
                        .append(Component.text(record.getAction().getX() + " " + record.getAction().getY() + " " + record.getAction().getZ()).color(NamedTextColor.YELLOW))
                        .append(Component.newline())
                        .append(Component.text("Item in hand: ").color(NamedTextColor.GREEN))
                        .append(Component.text(record.getAction().getActor().getItem()).color(NamedTextColor.YELLOW))
                        .build()
                    ).sendTo(context.cause().audience());

            // We should cancel an existing particle effect if there is one
            this.stopOutline(player);

            if (record.getAction() instanceof BlockAction action) {
                this.startOutline(player, action);
            }

            return CommandResult.success();
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException(e);
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

        Task task = Task.builder().execute(() -> {
            for (double[] offset : offsets) {
                player.spawnParticles(particleEffect, action.getServerLocation().position().add(offset[0], offset[1], offset[2]));
            }
        }).interval(1, TimeUnit.SECONDS).plugin(Espial.getInstance().getContainer()).build();

        ScheduledTask scheduledTask = Sponge.game().asyncScheduler().submit(task);
        Espial.getInstance().getBlockOutlines().put(player, scheduledTask);
    }

    private void stopOutline(Player player) {
        if (Espial.getInstance().getBlockOutlines().containsKey(player)) {
            Espial.getInstance().getBlockOutlines().get(player).cancel();
            Espial.getInstance().getBlockOutlines().remove(player);
        }
    }

    private void teleportPlayer(Player player, Action action) {
        Vector3d playerLocation = action.getActor().getPosition();
        Vector3d playerRotation = action.getActor().getRotation();

        if (playerLocation != null && playerRotation != null) {
            player.setPosition(playerLocation);
            player.setRotation(playerRotation);
        } else {
            player.setPosition(new Vector3d(action.getX(), action.getY(), action.getZ()));
        }
    }
}
