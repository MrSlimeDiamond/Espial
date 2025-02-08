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
import net.slimediamond.espial.*;
import net.slimediamond.espial.util.DisplayNameUtil;
import net.slimediamond.espial.util.DurationParser;
import net.slimediamond.espial.util.RayTraceUtil;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
        } else if (context.hasFlag("range")) {
            // -r <block range>
            int range = context.requireOne(CommandParameters.LOOKUP_RANGE);

            Vector3d pos = player.position();

            double minX = pos.x() - range;
            double minY = pos.y() - range;
            double minZ = pos.z() - range;

            double maxX = pos.x() + range;
            double maxY = pos.y() + range;
            double maxZ = pos.z() + range;

            ServerLocation min = ServerLocation.of(player.serverLocation().world(), new Vector3d(minX, minY, minZ));
            ServerLocation max = ServerLocation.of(player.serverLocation().world(), new Vector3d(maxX, maxY, maxZ));

            try {
                this.lookupRange(min, max, context);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        } else { // Ray trace block (playing is looking at target)
            // get the block the player is targeting

            Optional<LocatableBlock> result = RayTraceUtil.getBlockFacingPlayer(player);

            if (result.isPresent()) {
                LocatableBlock block = result.get();

                try {
                    this.lookupBlock(block.serverLocation(), context);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                context.sendMessage(Espial.prefix.append(Component.text("Could not detect a block. Move closer, perhaps?").color(NamedTextColor.RED)));
            }
        }

        return CommandResult.success();
    }

    private void lookupBlock(ServerLocation location, CommandContext context) throws SQLException {
        Espial.getInstance().getBlockLogService().processSingle(location, context, EspialTransactionType.LOOKUP);
    }

    protected void lookupRange(ServerLocation location, ServerLocation location2, CommandContext context) throws SQLException {
        Espial.getInstance().getBlockLogService().process(location, location2, context, EspialTransactionType.LOOKUP, true);
    }
}
