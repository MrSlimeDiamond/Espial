package net.slimediamond.espial.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.CommandParameters;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.action.BlockAction;
import net.slimediamond.espial.util.DisplayNameUtil;
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
            if (Espial.blockOutlines.containsKey(player)) {
                Espial.blockOutlines.get(player).cancel();
                Espial.blockOutlines.remove(player);
            }

            context.sendMessage(Component.text("No longer inspecting.").color(NamedTextColor.GREEN));

            return CommandResult.success();
        }

        int id = context.requireOne(CommandParameters.GENERIC_ID);

        try {
            BlockAction action = Espial.getInstance().getDatabase().queryId(id);

            if (action == null) {
                return CommandResult.error(Component.text("Unable to find a database index with that ID!"));
            }

            Vector3d playerLocation = action.getActorPosition();
            Vector3d playerRotation = action.getActorRotation();

            if (playerRotation != null && playerLocation != null) {
                player.setPosition(playerLocation);
                player.setRotation(playerRotation);
            } else {
                player.setPosition(new Vector3d(action.getX(), action.getY(), action.getZ()));
            }

            Component displayName = DisplayNameUtil.getDisplayName(action);
            String undoActionMessage;
            String undoCommand;
            if (action.isRolledBack()) {
                undoActionMessage = "RESTORE";
                undoCommand = "/espial restoreid " + action.getId();
            } else {
                undoActionMessage = "ROLLBACK";
                undoCommand = "/espial rollbackid " + action.getId();
            }

            PaginationList.builder()
                    .title(Component.text("Inspecting ID: ").color(NamedTextColor.GREEN).append(Component.text(id).color(NamedTextColor.YELLOW)))
                    .contents(Component.text("Information for this block:").color(NamedTextColor.GREEN)
                            .append(Component.newline())
                            .append(Component.text("[").color(NamedTextColor.GRAY).append(Component.text("STOP").color(NamedTextColor.RED).append(Component.text("]").color(NamedTextColor.GRAY))).clickEvent(ClickEvent.runCommand("/espial inspect stop")))
                            .append(Component.text(" [").color(NamedTextColor.GRAY).append(Component.text("TP").color(NamedTextColor.GREEN).append(Component.text("]").color(NamedTextColor.GRAY))).clickEvent(SpongeComponents.executeCallback(cause -> {
                                if (playerLocation != null && playerRotation != null) {
                                    player.setPosition(action.getActorPosition());
                                    player.setRotation(action.getActorRotation());
                                } else {
                                    player.setPosition(new Vector3d(action.getX(), action.getY(), action.getZ()));
                                }
                            })))
                            .append(Component.text(" [").color(NamedTextColor.GRAY).append(Component.text(undoActionMessage).color(NamedTextColor.YELLOW)).append(Component.text("]").color(NamedTextColor.GRAY))).clickEvent(ClickEvent.runCommand(undoCommand))
                            .append(Component.newline())
                            .append(Component.text("Player: ").color(NamedTextColor.GREEN))
                            .append(displayName)
                            .append(Component.newline())
                            .append(Component.text("Action: ").color(NamedTextColor.GREEN))
                            .append(Component.text(action.getActionType().toString()).color(NamedTextColor.YELLOW))
                            .append(Component.newline())
                            .append(Component.text("Coordinates: ").color(NamedTextColor.GREEN))
                            .append(Component.text(action.getX() + " " + action.getY() + " " + action.getZ()).color(NamedTextColor.YELLOW))
                            .append(Component.newline())
                            .append(Component.text("Tool: ").color(NamedTextColor.GREEN))
                            .append(Component.text(action.getActorItem()).color(NamedTextColor.YELLOW))
                    )
                    .sendTo((Audience) context.cause().root());

            // We should cancel an existing particle effect if there is one
            if (Espial.blockOutlines.containsKey(player)) {
                Espial.blockOutlines.get(player).cancel();
            }

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
            Espial.blockOutlines.put(player, scheduledTask);

            return CommandResult.success();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
