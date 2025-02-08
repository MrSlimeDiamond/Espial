package net.slimediamond.espial;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.util.DisplayNameUtil;
import net.slimediamond.espial.util.DurationParser;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class BlockLogService {
    private HashMap<Object, ArrayList<EspialTransaction>> transactions = new HashMap<>();

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

    public ActionStatus rollback(StoredBlock block) throws SQLException {
        if (block.rolledBack()) return ActionStatus.ALREADY_DONE;

        // roll back this specific ID to another state
        if (block.actionType() == ActionType.BREAK) {
            // place the block which was broken at that location
            BlockType blockType = BlockTypes.registry().value(ResourceKey.of(block.blockId().split(":")[0], block.blockId().split(":")[1]));

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

    public void process(ServerLocation min, ServerLocation max, CommandContext context, EspialTransactionType type, boolean isRange) {
        Timestamp timestamp = parseTimestamp(context, type);
        UUID uuid = parseFilter(context, "player", CommandParameters.LOOKUP_PLAYER);
        BlockState blockState = parseFilter(context, "block", CommandParameters.LOOKUP_BLOCK);

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
                Espial.getInstance().getBlockLogService().addTransaction(context.cause().root(), transaction);
            }

            sendResultMessage(context, blocks, type);
        } catch (SQLException e) {
            context.sendMessage(Espial.prefix.append(Component.text("A SQLException occurred when executing this. This is very very bad. The database is probably down. Look into this immediately!").color(NamedTextColor.RED)));
        }
    }

    public void processSingle(ServerLocation location, CommandContext context, EspialTransactionType type) {
        process(location, location, context, type, false);
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

    private void sendResultMessage(CommandContext context, List<StoredBlock> blocks, EspialTransactionType type) {
        String action = switch (type) {
            case ROLLBACK -> "rolled back";
            case RESTORE -> "restored";
            default -> "processed"; // fallback
        };

        if (type == EspialTransactionType.LOOKUP) {
            PaginationList.builder().contents(generateLookupContents(blocks, context.hasFlag("single")))
                    .title(Espial.prefix.append(Component.text("Block lookup results").color(NamedTextColor.WHITE))).sendTo(context.cause().audience());
            return;
        }

        if (blocks.isEmpty()) {
            context.sendMessage(Espial.prefix.append(Component.text("No actions were " + action + ".").color(NamedTextColor.WHITE)));
        } else {
            context.sendMessage(Espial.prefix.append(Component.text(blocks.size() + " action(s) " + action + ".").color(NamedTextColor.WHITE)));
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

                if (block.rolledBack()) {
                    msg.decorate(TextDecoration.STRIKETHROUGH);
                }

                contents.add(msg.build());
            });
        } else {
            // grouped
            Map<BlockAction, Integer> groupedBlocks = new HashMap<>();
            blocks.forEach(block -> {
                Component displayName = DisplayNameUtil.getDisplayName(block);

                BlockAction key = new BlockAction(displayName, block.actionType(), block.blockId());
                groupedBlocks.put(key, groupedBlocks.getOrDefault(key, 0) + 1);
            });

            groupedBlocks.forEach((key, count) -> {
                contents.add(Component.text()
                        .append(key.name())
                        .append(Component.space())
                        .append(Component.text(key.actionType().humanReadableVerb()).color(NamedTextColor.GREEN))
                        .append(Component.space())
                        .append(Component.text((count > 1 ? count + "x " : "")).color(NamedTextColor.WHITE))
                        .append(Component.text(key.blockId().split(":")[1]).color(NamedTextColor.GREEN))
                        .build()
                );
            });
        }

        return contents;
    }

    // Record for better key structure
    private record BlockAction(Component name, ActionType actionType, String blockId) {}

}
