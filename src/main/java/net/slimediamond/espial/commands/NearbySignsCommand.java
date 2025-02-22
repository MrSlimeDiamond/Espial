package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.CommandParameters;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.query.Sort;
import net.slimediamond.espial.util.BlockUtil;
import net.slimediamond.espial.util.MessageUtil;
import net.slimediamond.espial.util.PlayerSelectionUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerLocation;

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
                Query query = Query.builder()
                        .setType(QueryType.LOOKUP)
                        .setMin(locations.getLeft())
                        .setMax(locations.getRight())
                        .setSort(Sort.REVERSE_CHRONOLOGICAL)
                        .setUser(player)
                        .setAudience(player)
                        .build();

                List<BlockAction> signs = Espial.getInstance().getEspialService().query(query).stream().filter(action -> BlockUtil.SIGNS.contains(action.getBlockType())).toList();

                List<Component> contents = MessageUtil.generateLookupContents(signs, true);

                if (contents.isEmpty()) {
                    context.sendMessage(Espial.prefix.append(Component.text("Could not find any sign data nearby.").color(NamedTextColor.RED)));
                    return CommandResult.success();
                }

                PaginationList.builder()
                        .title(Espial.prefix.append(Component.text("Nearby signs").color(NamedTextColor.WHITE)))
                        .contents(contents)
                        .sendTo(player);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            context.sendMessage(Component.text("You must be a player to use this."));
        }

        return CommandResult.success();
    }
}
