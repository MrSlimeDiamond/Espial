package net.slimediamond.espial;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.action.ActionStatus;
import net.slimediamond.espial.action.ActionType;
import net.slimediamond.espial.nbt.NBTDataParser;
import net.slimediamond.espial.transaction.EspialTransaction;
import net.slimediamond.espial.transaction.EspialTransactionType;
import net.slimediamond.espial.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
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

public class BlockLogService {
    private final HashMap<Object, ArrayList<EspialTransaction>> transactions = new HashMap<>();
    private final ArrayList<UUID> inspectingPlayers = new ArrayList<>();

    public void addTransaction(Object key, EspialTransaction transaction) {
        if (this.transactions.containsKey(key)) {
            // add to the existing arraylist with a new transaction:
            this.transactions.get(key).add(transaction);
        } else {
            // create a new one with the source object
            ArrayList<EspialTransaction> transactions = new ArrayList<>();
            transactions.add(transaction);

            this.transactions.put(key, transactions);
        }
    }

    public HashMap<Object, ArrayList<EspialTransaction>> getTransactions() {
        return this.transactions;
    }

    public ArrayList<UUID> getInspectingPlayers() {
        return this.inspectingPlayers;
    }

    public ActionStatus rollback(StoredBlock block) throws SQLException {
        if (block.rolledBack()) return ActionStatus.ALREADY_DONE;

        // roll back this specific ID to another state
        if (block.actionType() == ActionType.BREAK) {
            // place the block which was broken at that location
            BlockType blockType = BlockTypes.registry().value(ResourceKey.of(block.blockId().split(":")[0], block.blockId().split(":")[1]));

//            if (blockType instanceof Sign sign) {
//                sign
//            }

            block.sponge().location().get().setBlock(blockType.defaultState());

            Espial.getInstance().getDatabase().setRolledBack(block.uid(), true);

            return ActionStatus.SUCCESS;
        } if (block.actionType() == ActionType.PLACE) {
            // EDGE CASE: We're always going to rollback places to air. This probably will cause no harm
            // since one must remove a block first before placing a block. But this might cause issues somehow, not sure.
            // (it'll be fine, probably)

            block.sponge().location().get().setBlock(BlockTypes.AIR.get().defaultState());
            Espial.getInstance().getDatabase().setRolledBack(block.uid(), true);
            return ActionStatus.SUCCESS;
        } else {
            return ActionStatus.UNSUPPORTED;
        }
    }

    public ActionStatus restore(StoredBlock block) throws SQLException {
        if (!block.rolledBack()) return ActionStatus.ALREADY_DONE;

        // roll forwards this specific ID to another state
        if (block.actionType() == ActionType.BREAK) {
            // place the block which was broken at that location

            block.sponge().location().get().setBlock(BlockTypes.AIR.get().defaultState());

            Espial.getInstance().getDatabase().setRolledBack(block.uid(), false);

            return ActionStatus.SUCCESS;
        } if (block.actionType() == ActionType.PLACE) {
            BlockType blockType = BlockTypes.registry().value(ResourceKey.of(block.blockId().split(":")[0], block.blockId().split(":")[1]));

            block.sponge().location().get().setBlock(blockType.defaultState());

            Espial.getInstance().getDatabase().setRolledBack(block.uid(), false);
            return ActionStatus.SUCCESS;
        } else {
            return ActionStatus.UNSUPPORTED;
        }
    }

    public void process(ServerLocation min, ServerLocation max, Audience audience, EspialTransactionType type, boolean isRange, @Nullable Timestamp timestamp, @Nullable UUID uuid, @Nullable BlockState blockState, boolean single) {
        String uuidString = (uuid == null) ? null : uuid.toString();
        String blockId = (blockState == null) ? null : blockState.asString();

        try {
            ArrayList<Integer> ids = new ArrayList<>();
            List<StoredBlock> blocks = isRange
                    ? Espial.getInstance().getDatabase().queryRange(min.world().key().formatted(), min.blockX(), min.blockY(), min.blockZ(), max.blockX(), max.blockY(), max.blockZ(), uuidString, blockId, timestamp)
                    : Espial.getInstance().getDatabase().queryBlock(min.world().key().formatted(), min.blockX(), min.blockY(), min.blockZ(), uuidString, blockId, timestamp);

            for (StoredBlock block : blocks) {
                if (block.rolledBack() && type == EspialTransactionType.ROLLBACK) continue;
                ids.add(block.uid());

                switch (type) {
                    case ROLLBACK:
                        Espial.getInstance().getBlockLogService().rollback(block);
                        break;
                    case RESTORE:
                        Espial.getInstance().getBlockLogService().restore(block);
                        break;
                    case LOOKUP:
                        // Lookup does not modify blocks
                        break;
                }
            }

            if (type != EspialTransactionType.LOOKUP) {
                EspialTransaction transaction = new EspialTransaction(ids, type, false);
                Espial.getInstance().getBlockLogService().addTransaction(audience, transaction);
            }

            sendResultMessage(audience, blocks, type, single);
        } catch (SQLException e) {
            audience.sendMessage(Espial.prefix.append(Component.text("A SQLException occurred when executing this. This is very very bad. The database is probably down. Look into this immediately!").color(NamedTextColor.RED)));
        }
    }

    public void processSingle(ServerLocation location, Audience audience, EspialTransactionType type, @Nullable Timestamp timestamp, @Nullable UUID uuid, @Nullable BlockState blockState, boolean single) {
        process(location, location, audience, type, false, timestamp, uuid, blockState, single);
    }

    private Timestamp parseTimestamp(CommandContext context, EspialTransactionType type) {
        if (context.hasFlag("time")) {
            String time = context.requireOne(CommandParameters.TIME);
            return new Timestamp(DurationParser.parseDurationAndSubtract(time));
        }
        if (type != EspialTransactionType.LOOKUP) {
            context.sendMessage(Espial.prefix.append(Component.text("Defaults used: -t 3d").color(NamedTextColor.GRAY)));
            return Timestamp.from(Instant.now().minus(3, ChronoUnit.DAYS));
        } else return Timestamp.from(Instant.ofEpochMilli(0)); // gotta catch 'em all!
    }

    private <T> T parseFilter(CommandContext context, String flag, Parameter.Value<T> parameter) {
        return context.hasFlag(flag) ? context.requireOne(parameter) : null;
    }

    private void sendResultMessage(Audience audience, List<StoredBlock> blocks, EspialTransactionType type, boolean single) {
        String action = switch (type) {
            case ROLLBACK -> "rolled back";
            case RESTORE -> "restored";
            default -> "processed"; // fallback
        };

        if (type == EspialTransactionType.LOOKUP) {
            ArrayList<Component> contents = generateLookupContents(blocks, single);

            if (contents.isEmpty()) {
                audience.sendMessage(Espial.prefix.append(Component.text("Could not find any block data for this location.").color(NamedTextColor.RED)));
                return;
            }

            PaginationList.builder().contents(contents)
                    .title(Espial.prefix.append(Component.text("Block lookup results").color(NamedTextColor.WHITE))).sendTo(audience);
            return;
        }

        if (blocks.isEmpty()) {
            audience.sendMessage(Espial.prefix.append(Component.text("No actions were " + action + ".").color(NamedTextColor.WHITE)));
        } else {
            audience.sendMessage(Espial.prefix.append(Component.text(blocks.size() + " action(s) " + action + ".").color(NamedTextColor.WHITE)));
        }
    }


    private ArrayList<Component> generateLookupContents(List<StoredBlock> blocks, boolean single) {
        ArrayList<Component> contents = new ArrayList<>();

        if (single) {
            blocks.forEach(block -> {
                Component displayName = DisplayNameUtil.getDisplayName(block);
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                String formattedDate = dateFormat.format(new Date(block.time().getTime()));
                var msg = Component.text()
                        .append(Component.text(formattedDate).color(NamedTextColor.GRAY))
                        .append(Component.space())
                        .append(displayName)
                        .append(Component.space())
                        .append(Component.text(block.actionType().humanReadableVerb()).color(NamedTextColor.GREEN))
                        .append(Component.space())
                        .append(Component.text(block.blockId().split(":")[1]).color(NamedTextColor.GREEN))
                        .clickEvent(ClickEvent.runCommand("/espial inspect " + block.uid()))
                        .hoverEvent(HoverEvent.showText(Espial.prefix
                                .append(Component.newline())
                                .append(Component.text("Click to teleport!").color(NamedTextColor.GRAY))
                                .append(Component.newline())
                                .append(Component.text("Internal ID: ").color(NamedTextColor.GRAY))
                                .append(Component.text(block.uid()).color(NamedTextColor.DARK_GRAY))
                                .append(Component.newline())
                                .append(Component.text("Item in hand: ").color(NamedTextColor.GRAY))
                                .append(Component.text(block.itemInHand()).color(NamedTextColor.DARK_GRAY))
                                .append(Component.newline())
                                .append(Component.text(formattedDate).color(NamedTextColor.DARK_GRAY))
                        ));

                block.getNBT().flatMap(data -> NBTDataParser.parseNBT(block)).ifPresent(component -> {
                    msg.append(Component.text(" (...)")
                            .color(NamedTextColor.GRAY)
                            .hoverEvent(HoverEvent.showText(Espial.prefix.append(
                                    Component.text().color(NamedTextColor.WHITE).append(component)))));
                });

                if (block.rolledBack()) {
                    msg.decorate(TextDecoration.STRIKETHROUGH);
                }
                contents.add(msg.build());
            });
        } else {
            // Grouped output in reverse chronological order
            Map<BlockAction, Integer> groupedBlocks = new HashMap<>();
            Map<BlockAction, Long> latestTimes = new HashMap<>();

            blocks.forEach(block -> {
                Component displayName = DisplayNameUtil.getDisplayName(block);
                BlockAction key = new BlockAction(displayName, block.actionType(), block.blockId());
                groupedBlocks.put(key, groupedBlocks.getOrDefault(key, 0) + 1);
                long time = block.time().getTime();
                latestTimes.put(key, Math.max(latestTimes.getOrDefault(key, 0L), time));
            });

            List<Map.Entry<BlockAction, Integer>> sortedEntries = new ArrayList<>(groupedBlocks.entrySet());
            sortedEntries.sort((e1, e2) ->
                    Long.compare(latestTimes.get(e2.getKey()), latestTimes.get(e1.getKey()))
            );

            sortedEntries.forEach(entry -> {
                BlockAction key = entry.getKey();
                int count = entry.getValue();
                contents.add(Component.text()
                        .append(key.name())
                        .append(Component.space())
                        .append(Component.text(key.actionType().humanReadableVerb()).color(NamedTextColor.GREEN))
                        .append(Component.space())
                        .append(Component.text((count > 1 ? count + "x " : "")).color(NamedTextColor.WHITE))
                        .append(Component.text(key.blockId().split(":")[1]).color(NamedTextColor.GREEN))
                        .build());
            });
        }
        return contents;
    }

    public CommandResult doSelectiveCommand(CommandContext context, EspialTransactionType type) {
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
        BlockState blockState = parseFilter(context, "block", CommandParameters.LOOKUP_BLOCK);


        if (context.hasFlag("worldedit")) { // Range lookup
            PlayerSelectionUtil.getWorldEditRegion(player).ifPresentOrElse(selection -> {
                context.sendMessage(Component.text().append(Espial.prefix).append(Component.text("Using your WorldEdit selection for this query.").color(NamedTextColor.WHITE)).build());
                Espial.getInstance().getBlockLogService().process(selection.getLeft(), selection.getRight(), context.cause().audience(), type, true, timestamp, uuid, blockState, context.hasFlag("single"));
            }, () -> {
                context.sendMessage(Espial.prefix.append(Component.text("You do not have a WorldEdit selection active!").color(NamedTextColor.RED)));
            });

            return CommandResult.success();
        } else if (context.hasFlag("range")) {
            // -r <block range>
            int range = context.requireOne(CommandParameters.LOOKUP_RANGE);

            Pair<ServerLocation, ServerLocation> selection = PlayerSelectionUtil.getCuboidAroundPlayer(player, range);

            context.sendMessage(Component.text().append(Espial.prefix).append(Component.text("Using a cuboid with a range of " + range + " blocks for this query.").color(NamedTextColor.WHITE)).build());

            Espial.getInstance().getBlockLogService().process(selection.getLeft(), selection.getRight(), context.cause().audience(), type, true, timestamp, uuid, blockState, context.hasFlag("single"));

        } else {
            // Ray trace block (playing is looking at target)
            // get the block the player is targeting

            Optional<LocatableBlock> result = RayTraceUtil.getBlockFacingPlayer(player);

            if (result.isPresent()) {
                LocatableBlock block = result.get();

                Espial.getInstance().getBlockLogService().processSingle(block.serverLocation(), context.cause().audience(), type, timestamp, uuid, blockState, context.hasFlag("single"));
            } else {
                context.sendMessage(Espial.prefix.append(Component.text("Could not detect a block. Move closer, perhaps?").color(NamedTextColor.RED)));
            }
        }

        return CommandResult.success();
    }

    // Record for better key structure
    private record BlockAction(Component name, ActionType actionType, String blockId) {}

}
