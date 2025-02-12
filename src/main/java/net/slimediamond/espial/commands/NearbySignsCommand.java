package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.CommandParameters;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.action.BlockAction;
import net.slimediamond.espial.transaction.EspialTransactionType;
import net.slimediamond.espial.util.BlockUtil;
import net.slimediamond.espial.util.PlayerSelectionUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.util.List;

public class NearbySignsCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        // FIXME: Query only for signs in the database
        if (context.cause().root() instanceof Player player) {
            int range;
            if (context.hasFlag("range")) {
                range = context.requireOne(CommandParameters.LOOKUP_RANGE);
            } else {
                // Default to 5 blocks
                context.sendMessage(Espial.prefix.append(Component.text("Defaults used: -r 5").color(NamedTextColor.GRAY)));
                range = 5;
            }

            context.sendMessage(Component.text().append(Espial.prefix).append(Component.text("Using a cuboid with a range of " + range + " blocks for this query.").color(NamedTextColor.WHITE)).build());

            Pair<ServerLocation, ServerLocation> locations = PlayerSelectionUtil.getCuboidAroundPlayer(player, range);
            try {
                List<BlockAction> signs = Espial.getInstance().getDatabase().queryRange(
                        locations.getLeft().worldKey().formatted(),
                        locations.getLeft().blockX(),
                        locations.getLeft().blockY(),
                        locations.getLeft().blockZ(),
                        locations.getRight().blockX(),
                        locations.getRight().blockY(),
                        locations.getRight().blockZ(),
                        null, null, null
                ).stream().filter(action -> BlockUtil.SIGNS.contains(action.getBlockType())).toList();

                Espial.getInstance().getBlockLogService().sendResultMessage(context.cause().audience(), signs, EspialTransactionType.LOOKUP, true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            context.sendMessage(Component.text("You must be a player to use this."));
        }

        return CommandResult.success();
    }
}
