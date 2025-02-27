package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.util.RayTraceUtil;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class IsThisBlockMineCommand implements CommandExecutor {
  @Override
  public CommandResult execute(CommandContext context) throws CommandException {
    if (context.cause().root() instanceof Player player) {
      RayTraceUtil.getBlockFacingPlayer(player)
          .ifPresentOrElse(block -> {
                try {
                  Espial.getInstance()
                      .getDatabase()
                      .getBlockOwner(
                          block.serverLocation().worldKey().formatted(),
                          block.location().blockX(),
                          block.location().blockY(),
                          block.location().blockZ())
                      .ifPresentOrElse(user -> {
                            if (user.profile().uuid().equals(player.profile().uuid())) {
                              context.sendMessage(
                                  Component.text("This block was placed by you!")
                                      .color(NamedTextColor.GREEN));
                            } else {
                              // We have a user, but it's not them, so it's not their block!
                              context.sendMessage(
                                  Component.text("This block was placed by another player.")
                                      .color(NamedTextColor.YELLOW));
                            }
                          },
                          () -> {
                            // No user can be found for this block. It's probably not in the
                            // database.
                            context.sendMessage(
                                Component.text("We are not sure who placed this block.")
                                    .color(NamedTextColor.RED));
                          });
                } catch (SQLException | ExecutionException | InterruptedException e) {
                  context.sendMessage(
                      Component.text(
                              "Unfortunately, an error occurred when looking up this block's owner. " +
                                      "If this persists, tell an admin!")
                          .color(NamedTextColor.RED));
                  throw new RuntimeException(e);
                }
              },
              () -> {
                context.sendMessage(
                    Component.text(
                            "Could not find the block in front of you, " +
                                    "perhaps try to move closer?")
                        .color(NamedTextColor.RED));
              });
    }
    return CommandResult.success();
  }
}
