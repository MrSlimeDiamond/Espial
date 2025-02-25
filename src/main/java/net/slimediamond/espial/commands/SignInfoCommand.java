package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.nbt.NBTDataParser;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.record.BlockRecord;
import net.slimediamond.espial.util.BlockUtil;
import net.slimediamond.espial.util.Format;
import net.slimediamond.espial.util.RayTraceUtil;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.List;

public class SignInfoCommand implements CommandExecutor {
  @Override
  public CommandResult execute(CommandContext context) throws CommandException {
    if (context.cause().root() instanceof Player player) {
      RayTraceUtil.getBlockFacingPlayer(player)
          .ifPresentOrElse(
              block -> {
                if (BlockUtil.SIGNS.contains(block.blockState().type())) {
                  // player is looking at a sign
                  ServerLocation location = block.serverLocation();
                  String blockId =
                      RegistryTypes.BLOCK_TYPE
                          .get()
                          .valueKey(block.blockState().type())
                          .formatted();

                  try {
                    Query query =
                        Query.builder()
                            .type(QueryType.LOOKUP)
                            .min(location)
                            .blocks(List.of(blockId))
                            .caller(player)
                            .audience(player)
                            .build();

                    List<BlockRecord> blocks =
                        Espial.getInstance().getEspialService().query(query).stream()
                            .filter(record -> record instanceof BlockRecord)
                            .map(record -> (BlockRecord) record) // Cast safely
                            .filter(
                                blockRecord ->
                                    BlockUtil.SIGNS.contains(
                                        ((BlockAction) blockRecord.getAction()).getBlockType()))
                            .toList();

                    BlockRecord target = blocks.get(0); // top index

                    Component name = Format.getDisplayName(target.getAction());

                    var builder =
                        Component.text()
                            .append(
                                Format.component(
                                    Component.text("That sign was last modified by ")
                                        .color(NamedTextColor.WHITE)
                                        .append(name.color(NamedTextColor.YELLOW))));

                    var info = Component.text().append(Format.prefix);

                    info.append(Component.newline());
                    info.append(
                        Component.text("Date: ")
                            .color(NamedTextColor.DARK_AQUA)
                            .append(
                                Component.text(Format.date(target.getTimestamp()))
                                    .color(NamedTextColor.WHITE)));

                    ((BlockAction) target.getAction())
                        .getNBT()
                        .flatMap(NBTDataParser::parseNBT)
                        .ifPresent(info::append);

                    builder.append(
                        Component.text(" (...)")
                            .color(NamedTextColor.GRAY)
                            .hoverEvent(HoverEvent.showText(info)));
                    context.sendMessage(builder.build());

                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }

                } else {
                  context.sendMessage(
                      Format.error("The block you are looking at is not a sign."));
                }
              },
              () -> {
                context.sendMessage(Format.noBlockFound());
              });
    } else {
      context.sendMessage(Format.playersOnly());
    }

    return CommandResult.success();
  }
}
