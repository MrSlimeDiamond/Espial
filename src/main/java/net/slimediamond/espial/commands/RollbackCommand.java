package net.slimediamond.espial.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.sponge.SpongeAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Database;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.Parameters;
import net.slimediamond.espial.StoredBlock;
import net.slimediamond.espial.util.DurationParser;
import net.slimediamond.espial.util.RayTraceUtil;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class RollbackCommand implements CommandExecutor {
    private Database database;

    public RollbackCommand(Database database) {
        this.database = database;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
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
            }

            return CommandResult.success();
        } else if (context.hasFlag("range")) {
            // -r <block range>
            int range = context.requireOne(Parameters.LOOKUP_RANGE);

            Vector3d pos = player.position();

            double minX = pos.x() - range;
            double minY = pos.y() - range;
            double minZ = pos.z() - range;

            double maxX = pos.x() + range;
            double maxY = pos.y() + range;
            double maxZ = pos.z() + range;

            ServerLocation min = ServerLocation.of(player.serverLocation().world(), new Vector3d(minX, minY, minZ));
            ServerLocation max = ServerLocation.of(player.serverLocation().world(), new Vector3d(maxX, maxY, maxZ));

            this.lookupRange(min, max, context);

        } else { // Ray trace block (playing is looking at target)
            // get the block the player is targeting

            Optional<LocatableBlock> result = RayTraceUtil.getBlockFacingPlayer(player);

            if (result.isPresent()) {
                LocatableBlock block = result.get();

                this.lookupBlock(block.serverLocation(), context);
            } else {
                context.sendMessage(Espial.prefix.append(Component.text("Could not detect a block. Move closer, perhaps?").color(NamedTextColor.RED)));
            }
        }

        return CommandResult.success();
    }

    protected void lookupRange(ServerLocation min, ServerLocation max, CommandContext context) {
        // Default to 3 days ago
        Timestamp timestamp = Timestamp.from(Instant.now().minus(3, ChronoUnit.DAYS));

        String uuid = null;
        String blockId = null;

        if (context.hasFlag("player")) {
            // Get the UUID of the player (if available!)
            uuid = context.requireOne(Parameters.LOOKUP_PLAYER).toString();
        }

        if (context.hasFlag("block")) {
            // Get the UUID of the player (if available!)
            blockId = context.requireOne(Parameters.LOOKUP_BLOCK).type().key(RegistryTypes.BLOCK_TYPE).formatted();
        }

        if (context.hasFlag("time")) {
            String time = context.requireOne(Parameters.TIME);
            // translated into long (ms)
            timestamp = new Timestamp(DurationParser.parseDurationAndSubtract(time));
        } else {
            context.sendMessage(Espial.prefix.append(Component.text("Defaults used: ").color(NamedTextColor.WHITE).append(Component.text("-t 3d").color(NamedTextColor.GRAY))));
        }

        try {
            int actions = 0;
            for (StoredBlock block : database.queryRange(min.world().key().formatted(), min.blockX(), min.blockY(), min.blockZ(), max.blockX(), max.blockY(), max.blockZ(), uuid, blockId, timestamp)) {
                if (block.rolledBack()) continue;
                actions++;

                Espial.getInstance().rollback(block);
            }

            if (actions == 0) {
                context.sendMessage(Espial.prefix.append(Component.text("Nothing was rolled back.").color(NamedTextColor.WHITE)));
            } else {
                context.sendMessage(Espial.prefix.append(Component.text().append(Component.text(actions).append(Component.text(" action(s) were rolled back."))).color(NamedTextColor.WHITE)));
            }
        } catch (SQLException e) {
            context.sendMessage(Espial.prefix.append(Component.text("A SQLException occurred when executing this. This is very very bad. The database is probably down. Look into this immediately.").color(NamedTextColor.RED)));
        }
    }

    protected void lookupBlock(ServerLocation location, CommandContext context) {
        // Default to 3 days ago
        Timestamp timestamp = Timestamp.from(Instant.now().minus(3, ChronoUnit.DAYS));

        String uuid = null;
        String blockId = null;

        if (context.hasFlag("player")) {
            // Get the UUID of the player (if available!)
            uuid = context.requireOne(Parameters.LOOKUP_PLAYER).toString();
        }

        if (context.hasFlag("block")) {
            // Get the UUID of the player (if available!)
            blockId = context.requireOne(Parameters.LOOKUP_BLOCK).type().key(RegistryTypes.BLOCK_TYPE).formatted();
        }

        if (context.hasFlag("time")) {
            String time = context.requireOne(Parameters.TIME);
            // translated into long (ms)
            timestamp = new Timestamp(DurationParser.parseDurationAndSubtract(time));
        } else {
            context.sendMessage(Espial.prefix.append(Component.text("Defaults used: ").color(NamedTextColor.WHITE).append(Component.text("-t 3d").color(NamedTextColor.GRAY))));
        }

        try {
            int actions = 0;
            for (StoredBlock block : database.queryBlock(location.world().key().formatted(), location.blockX(), location.blockY(), location.blockZ(), uuid, blockId, timestamp)) {
                if (block.rolledBack()) continue;
                actions++;

                Espial.getInstance().rollback(block);
            }

            if (actions == 0) {
                context.sendMessage(Espial.prefix.append(Component.text("Nothing was rolled back.").color(NamedTextColor.WHITE)));
            } else {
                context.sendMessage(Espial.prefix.append(Component.text().append(Component.text(actions).append(Component.text(" action(s) were rolled back."))).color(NamedTextColor.WHITE)));
            }
        } catch (SQLException e) {
            context.sendMessage(Espial.prefix.append(Component.text("A SQLException occurred when executing this. This is very very bad. The database is probably down. Look into this immediately.").color(NamedTextColor.RED)));
        }
    }
}
