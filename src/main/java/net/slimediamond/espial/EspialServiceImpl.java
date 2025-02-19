package net.slimediamond.espial;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.api.EspialService;
import net.slimediamond.espial.api.action.ActionType;
import net.slimediamond.espial.api.action.BlockAction;
import net.slimediamond.espial.api.nbt.NBTDataParser;
import net.slimediamond.espial.api.query.Query;
import net.slimediamond.espial.api.query.QueryType;
import net.slimediamond.espial.api.transaction.EspialTransaction;
import net.slimediamond.espial.api.transaction.EspialTransactionType;
import net.slimediamond.espial.api.transaction.TransactionStatus;
import net.slimediamond.espial.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class EspialServiceImpl implements EspialService {

    private <T> T parseFilter(CommandContext context, String flag, Parameter.Value<T> parameter) {
        return context.hasFlag(flag) ? context.requireOne(parameter) : null;
    }

    private Timestamp parseTimestamp(CommandContext context, QueryType type) {
        if (context.hasFlag("time")) {
            String time = context.requireOne(CommandParameters.TIME);
            return new Timestamp(DurationParser.parseDurationAndSubtract(time));
        }
        if (type != QueryType.LOOKUP) {
            context.sendMessage(Espial.prefix.append(Component.text("Defaults used: -t 3d").color(NamedTextColor.GRAY)));
            return Timestamp.from(Instant.now().minus(3, ChronoUnit.DAYS));
        } else return Timestamp.from(Instant.ofEpochMilli(0)); // gotta catch 'em all!
    }

    @Override
    public TransactionStatus execute(EspialTransaction transaction) {
        return null;
    }

    @Override
    public void setSignData(BlockAction action) {
        action.getServerLocation().blockEntity().ifPresent(tileEntity -> {
            action.getNBT().ifPresent(nbtData -> {
                if (nbtData.getSignData() != null) {
                    List<Component> components = new ArrayList<>();

                    nbtData.getSignData().getFrontText().forEach(line -> components.add(GsonComponentSerializer.gson().deserialize(line)));

                    tileEntity.offer(Keys.SIGN_LINES, components);
                }
            });
        });
    }

    @Override
    public ArrayList<BlockAction> query(Query query) throws SQLException {
        return Espial.getInstance().getDatabase().query(query);
    }

    @Override
    public ArrayList<Component> generateLookupContents(List<BlockAction> actions, boolean spread) {
        ArrayList<Component> contents = new ArrayList<>();

        if (spread) {
            actions.forEach(block -> {
                Component displayName = DisplayNameUtil.getDisplayName(block);
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                String formattedDate = dateFormat.format(new Date(block.getTimestamp().getTime()));
                var msg = Component.text()
                        .append(Component.text(formattedDate).color(NamedTextColor.GRAY))
                        .append(Component.space())
                        .append(displayName)
                        .append(Component.space())
                        .append(Component.text(block.getActionType().getHumanReadableVerb()).color(NamedTextColor.GREEN))
                        .append(Component.space())
                        .append(Component.text(block.getBlockId().split(":")[1]).color(NamedTextColor.GREEN))
                        .clickEvent(ClickEvent.runCommand("/espial inspect " + block.getId()))
                        .hoverEvent(HoverEvent.showText(Espial.prefix
                                .append(Component.newline())
                                .append(Component.text("Click to teleport!").color(NamedTextColor.GRAY))
                                .append(Component.newline())
                                .append(Component.text("Internal ID: ").color(NamedTextColor.GRAY))
                                .append(Component.text(block.getId()).color(NamedTextColor.DARK_GRAY))
                                .append(Component.newline())
                                .append(Component.text("Item in hand: ").color(NamedTextColor.GRAY))
                                .append(Component.text(block.getActorItem()).color(NamedTextColor.DARK_GRAY))
                                .append(Component.newline())
                                .append(Component.text(formattedDate).color(NamedTextColor.DARK_GRAY))
                        ));

                block.getNBT().flatMap(NBTDataParser::parseNBT).ifPresent(component -> {
                    msg.append(Component.text(" (...)")
                            .color(NamedTextColor.GRAY)
                            .hoverEvent(HoverEvent.showText(Espial.prefix.append(
                                    Component.text().color(NamedTextColor.WHITE).append(component)))));
                });

                if (block.isRolledBack()) {
                    msg.decorate(TextDecoration.STRIKETHROUGH);
                }
                contents.add(msg.build());
            });
        } else {
            // Grouped output in reverse chronological order
            Map<BlockTracker, Integer> groupedBlocks = new HashMap<>();
            Map<BlockTracker, Long> latestTimes = new HashMap<>();

            actions.forEach(block -> {
                Component displayName = DisplayNameUtil.getDisplayName(block);
                BlockTracker key = new BlockTracker(displayName, block.getActionType(), block.getBlockId());
                groupedBlocks.put(key, groupedBlocks.getOrDefault(key, 0) + 1);
                long time = block.getTimestamp().getTime();
                latestTimes.put(key, Math.max(latestTimes.getOrDefault(key, 0L), time));
            });

            List<Map.Entry<BlockTracker, Integer>> sortedEntries = new ArrayList<>(groupedBlocks.entrySet());
            sortedEntries.sort((e1, e2) ->
                    Long.compare(latestTimes.get(e2.getKey()), latestTimes.get(e1.getKey()))
            );

            sortedEntries.forEach(entry -> {
                BlockTracker key = entry.getKey();
                int count = entry.getValue();
                contents.add(Component.text()
                        .append(key.name())
                        .append(Component.space())
                        .append(Component.text(key.actionType().getHumanReadableVerb()).color(NamedTextColor.GREEN))
                        .append(Component.space())
                        .append(Component.text((count > 1 ? count + "x " : "")).color(NamedTextColor.WHITE))
                        .append(Component.text(key.blockId().split(":")[1]).color(NamedTextColor.GREEN))
                        .build());
            });
        }
        return contents;
    }

    @Override
    public CommandResult execute(CommandContext context, QueryType type) {
        Player player;
        if (context.cause().root() instanceof Player) {
            player = (Player) context.cause().root();
        } else {
            return CommandResult.error(Component.text("This command can only be used by players.").color(NamedTextColor.RED));
        }

        Timestamp timestamp;
        try {
            timestamp = parseTimestamp(context, type);
        } catch (IllegalArgumentException e) {
            context.sendMessage(Espial.prefix.append(Component.text().append(Component.text("Could not parse time argument '").append(Component.text(context.requireOne(CommandParameters.TIME)).append(Component.text("'.")))).color(NamedTextColor.RED)));
            return CommandResult.success();
        }
        UUID uuid = parseFilter(context, "player", CommandParameters.LOOKUP_PLAYER);

        Query.Builder builder = Query.builder()
                .setType(type)
                .setPlayerUUID(uuid)
                .setTimestamp(timestamp);

        BlockState blockState = parseFilter(context, "block", CommandParameters.LOOKUP_BLOCK);
        if (blockState != null) {
            String blockId = RegistryTypes.BLOCK_TYPE.get().valueKey(blockState.type()).formatted();
            builder.setBlockId(blockId);
        }

        if (context.hasFlag("worldedit")) { // Range lookup
            try {
                WorldEditSelectionUtil.getWorldEditRegion(player).ifPresentOrElse(selection -> {
                    context.sendMessage(Component.text().append(Espial.prefix).append(Component.text("Using your WorldEdit selection for this query.").color(NamedTextColor.WHITE)).build());
                    builder.setMin(selection.getLeft());
                    builder.setMax(selection.getRight());

                    }, () -> {
                    context.sendMessage(Espial.prefix.append(Component.text("You do not have a WorldEdit selection active!").color(NamedTextColor.RED)));
                });
            } catch (NoClassDefFoundError e) {
                context.sendMessage(Component.text("It doesn't look like WorldEdit is installed on this server!").color(NamedTextColor.RED));
            }

            return CommandResult.success();
        } else if (context.hasFlag("range")) {
            // -r <block range>
            int range = context.requireOne(CommandParameters.LOOKUP_RANGE);

            Pair<ServerLocation, ServerLocation> selection = PlayerSelectionUtil.getCuboidAroundPlayer(player, range);

            context.sendMessage(Component.text().append(Espial.prefix).append(Component.text("Using a cuboid with a range of " + range + " blocks for this query.").color(NamedTextColor.WHITE)).build());

            builder.setMin(selection.getLeft());
            builder.setMin(selection.getRight());
        } else {
            // Ray trace block (playing is looking at target)
            // get the block the player is targeting

            Optional<LocatableBlock> result = RayTraceUtil.getBlockFacingPlayer(player);

            if (result.isPresent()) {
                LocatableBlock block = result.get();

                builder.setMin(block.serverLocation());
            } else {
                context.sendMessage(Espial.prefix.append(Component.text("Could not detect a block. Move closer, perhaps?").color(NamedTextColor.RED)));
            }

            try {
                this.process(builder.build(), context.cause().audience(), context.hasFlag("s"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return CommandResult.success();
    }

    public TransactionStatus rollback(BlockAction action) throws SQLException {
        if (action.isRolledBack()) return TransactionStatus.ALREADY_DONE;

        // roll back this specific ID to another state
        if (action.getActionType() == ActionType.BREAK) {
            // place the block which was broken at that location

            action.getServerLocation().setBlock(action.getState());

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {
                setSignData(action);
            }

            Espial.getInstance().getDatabase().setRolledBack(action.getId(), true);

            return TransactionStatus.SUCCESS;
        } if (action.getActionType() == ActionType.PLACE) {
            // EDGE CASE: We're always going to rollback places to air. This probably will cause no harm
            // since one must remove a block first before placing a block. But this might cause issues somehow, not sure.
            // (it'll be fine, probably)

            action.getServerLocation().setBlock(BlockTypes.AIR.get().defaultState());
            Espial.getInstance().getDatabase().setRolledBack(action.getId(), true);
            return TransactionStatus.SUCCESS;
        } else if (action.getActionType() == ActionType.MODIFY) {
            // Rolling back a modification action will entail going to its previous state of modification
            // (if it's present), so let's look for that.

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {

                BlockState state = action.getState();
                action.getServerLocation().setBlock(state);

                List<BlockAction> actions = this.query(Query.builder().setMin(action.getServerLocation()).build()).stream().filter(a -> !a.isRolledBack()).toList();
                if (actions.size() >= 2) {
                    setSignData(actions.get(1));
                }

                Espial.getInstance().getDatabase().setRolledBack(action.getId(), true);

                return TransactionStatus.SUCCESS;
            }
        }
        return TransactionStatus.UNSUPPORTED;
    }

    public TransactionStatus restore(BlockAction action) throws SQLException {
        if (!action.isRolledBack()) return TransactionStatus.ALREADY_DONE;

        // roll forwards this specific ID to another state
        if (action.getActionType() == ActionType.BREAK) {
            // place the block which was broken at that location

            action.getServerLocation().setBlock(BlockTypes.AIR.get().defaultState());

            Espial.getInstance().getDatabase().setRolledBack(action.getId(), false);

            return TransactionStatus.SUCCESS;
        } if (action.getActionType() == ActionType.PLACE) {
            action.getServerLocation().setBlock(action.getState());

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {
                setSignData(action);
            }

            Espial.getInstance().getDatabase().setRolledBack(action.getId(), false);
            return TransactionStatus.SUCCESS;
        } if (action.getActionType() == ActionType.MODIFY) {
            // Because this is a restore, let's get the one after this which is rolled back

            if (BlockUtil.SIGNS.contains(action.getBlockType())) {

                BlockState state = action.getState();
                action.getServerLocation().setBlock(state);

                List<BlockAction> actions = this.query(Query.builder().setMin(action.getServerLocation()).build()).stream().filter(a -> a.isRolledBack()).toList();

                if (actions.size() >= 2) {
                    setSignData(actions.get(1));
                }

                Espial.getInstance().getDatabase().setRolledBack(action.getId(), false);

                return TransactionStatus.SUCCESS;
            }

        }

        return TransactionStatus.UNSUPPORTED;
    }

    public void process(Query query, Audience audience, boolean spread) throws Exception {
        ArrayList<BlockAction> actions = this.query(query);
        switch (query.getType()) {
            case ROLLBACK -> this.rollbackAll(actions);
            case RESTORE -> this.restoreAll(actions);
        }

        if (query.getType() == QueryType.LOOKUP) {
            PaginationList.builder().title(Espial.prefix.append(Component.text("Lookup results").color(NamedTextColor.WHITE)))
                    .contents(this.generateLookupContents(actions, spread))
                    .sendTo(audience);
        } else if (query.getType() == QueryType.ROLLBACK) {
            audience.sendMessage(Espial.prefix.append(Component.text(actions.size() + " action(s) were rolled back.").color(NamedTextColor.WHITE)));
        } else if (query.getType() == QueryType.RESTORE) {
            audience.sendMessage(Espial.prefix.append(Component.text(actions.size() + " action(s) were restored.").color(NamedTextColor.WHITE)));

        }
    }

    private record BlockTracker(Component name, ActionType actionType, String blockId) {}
}
