package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.slimediamond.espial.CommandParameters;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.query.Sort;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.util.BlockUtil;
import net.slimediamond.espial.util.Format;
import net.slimediamond.espial.util.PlayerSelectionUtil;
import net.slimediamond.espial.util.SpongeUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Comparator;
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
        context.sendMessage(Format.defaults("-r 5"));
        range = 5;
      }

      Pair<ServerLocation, ServerLocation> locations =
          PlayerSelectionUtil.getCuboidAroundPlayer(player, range);
      try {
        List<EspialRecord> signs =
            Espial.getInstance()
                .getEspialService()
                .query(
                    Query.builder()
                        .type(QueryType.LOOKUP)
                        .min(locations.getLeft())
                        .max(locations.getRight())
                        .caller(player)
                        .sort(Sort.REVERSE_CHRONOLOGICAL)
                        .blocks(
                            BlockUtil.SIGNS.stream()
                                .map(SpongeUtil::getBlockId)
                                .toList())
                        .audience(player)
                        .spread(true)
                        .build());

        List<Component> contents = Format.generateLookupContents(signs, true);

        if (contents.isEmpty()) {
          context.sendMessage(Format.error("Could not find any signs nearby."));
          return CommandResult.success();
        }

        PaginationList.builder()
            .title(Format.title("Nearby Signs"))
            .padding(Format.PADDING)
            .contents(contents)
            .sendTo(player);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      context.sendMessage(Format.playersOnly());
    }

    return CommandResult.success();
  }
}
