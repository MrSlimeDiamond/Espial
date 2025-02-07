package net.slimediamond.espial.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.sponge.SpongeAdapter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.slimediamond.espial.ActionType;
import net.slimediamond.espial.Database;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.StoredBlock;
import net.slimediamond.espial.util.DisplayNameUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class LookupCommand implements CommandExecutor {
    Database database;

    public LookupCommand(Database database) {
        this.database = database;
    }

    @Override
    public CommandResult execute(CommandContext context) {
        Player player;
        if (context.cause().root() instanceof Player) {
            player = (Player) context.cause().root();
        } else {
            return CommandResult.error(Component.text("This command can only be used by players.").color(NamedTextColor.RED));
        }

        if (context.hasFlag("worldedit")) { // Range lookup
            WorldEdit worldEdit = WorldEdit.getInstance();
            com.sk89q.worldedit.entity.Player wePlayer = SpongeAdapter.adapt((ServerPlayer) player);

            LocalSession localSession = worldEdit.getSessionManager().get(wePlayer);

            try {
                Region region = localSession.getSelection(localSession.getSelectionWorld());
                if (region == null) {
                    context.sendMessage(Component.text("No WorldEdit region selected!").color(NamedTextColor.RED));
                    return CommandResult.success();
                } else {
                    BlockVector3 regionMin = region.getMinimumPoint();
                    BlockVector3 regionMax = region.getMaximumPoint();

                    Vector3d regionMin3d = new Vector3d(regionMin.getX(), regionMin.getY(), regionMin.getZ());
                    Vector3d regionMax3d = new Vector3d(regionMax.getX(), regionMax.getY(), regionMax.getZ());

                    ServerLocation serverLocation = ServerLocation.of(player.serverLocation().world(), regionMin3d);
                    ServerLocation serverLocation2 = ServerLocation.of(player.serverLocation().world(), regionMax3d);

                    this.lookupRange(serverLocation, serverLocation2, context);
                }
            } catch (IncompleteRegionException e) {
                context.sendMessage(Component.text("No WorldEdit region selected!").color(NamedTextColor.RED));
                return CommandResult.success();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return CommandResult.success();
        } else { // Ray trace block (playing is looking at target)
            // get the block the player is targeting

            Optional<RayTraceResult<LocatableBlock>> result = RayTrace.block().sourceEyePosition(player).direction(player).world(player.serverLocation().world()).limit(4).execute();

            if (result.isPresent()) {
                RayTraceResult<LocatableBlock> rayTrace = result.get();

                try {
                    this.lookupBlock(rayTrace.selectedObject().serverLocation(), context);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                context.sendMessage(Component.text("Could not detect a block. Move closer, perhaps?").color(NamedTextColor.RED));
            }
        }

        return CommandResult.success();
    }

    private void lookupBlock(ServerLocation location, CommandContext context) throws SQLException {
        ArrayList<StoredBlock> blocks = database.queryBlock(location.world().key().formatted(), location.blockX(), location.blockY(), location.blockZ());
        PaginationList.Builder paginationListBuilder = PaginationList.builder()
                .title(Component.text().color(NamedTextColor.DARK_GRAY).append(Espial.prefix)
                        .append(Component.text("Block data at ").color(NamedTextColor.GRAY)
                        .append(Component.text(location.blockX() + " " + location.blockY() + " " + location.blockZ())
                                .color(NamedTextColor.YELLOW))
                ).build());

        ArrayList<Component> contents = this.generateContents(blocks, context.hasFlag("single"));

        paginationListBuilder.contents(contents);
        paginationListBuilder.sendTo((Audience) context.cause().root());
    }

    protected void lookupRange(ServerLocation location, ServerLocation location2, CommandContext context) throws SQLException {
        ArrayList<StoredBlock> blocks = database.queryRange(location.world().key().formatted(), location.blockX(), location.blockY(), location.blockZ(), location2.blockX(), location2.blockY(), location2.blockZ());

        PaginationList.Builder paginationListBuilder = PaginationList.builder().title(
                Component.text().color(NamedTextColor.DARK_GRAY).append(Espial.prefix)
                .append(Component.text("Block data between ")
                        .color(NamedTextColor.GRAY)
                        .append(Component.text(location.blockX() + " " + location.blockY() + " " + location.blockZ())
                                .color(NamedTextColor.YELLOW))
                        .append(Component.text(" and "))
                        .color(NamedTextColor.GRAY)
                        .append(Component.text(location2.blockX() + " " + location2.blockY() + " " + location2.blockZ())
                                .color(NamedTextColor.YELLOW))
                ).build());

        ArrayList<Component> contents = this.generateContents(blocks, context.hasFlag("single"));

        paginationListBuilder.contents(contents);
        paginationListBuilder.sendTo((Audience) context.cause().root());
    }

    protected ArrayList<Component> generateContents(ArrayList<StoredBlock> blocks, boolean single) {
        ArrayList<Component> contents = new ArrayList<>();

        if (single) {
            blocks.forEach(block -> {
                Component displayName = DisplayNameUtil.getDisplayName(block);

                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");

                contents.add(Component.text()
                        .append(Component.text(dateFormat.format(new Date(block.time().getTime() * 1000))).color(NamedTextColor.GRAY))
                        .append(Component.space())
                        .append(displayName)
                        .append(Component.space())
                        .append(Component.text(block.actionType().humanReadableVerb()).color(NamedTextColor.GREEN))
                        .append(Component.space())
                        .append(Component.text(block.blockId().split(":")[1]).color(NamedTextColor.GREEN))
                        .clickEvent(ClickEvent.runCommand("/espial inspect " + block.uid()))
                        .build()
                );
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
