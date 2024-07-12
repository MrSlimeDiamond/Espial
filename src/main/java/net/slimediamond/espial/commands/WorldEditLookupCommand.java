package net.slimediamond.espial.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.session.SessionOwner;
import com.sk89q.worldedit.sponge.SpongeAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Database;
import net.slimediamond.espial.StoredBlock;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.sql.SQLException;
import java.util.ArrayList;

public class WorldEditLookupCommand extends LookupCommand {
    public WorldEditLookupCommand(Parameter.Value<ServerLocation> locationParameter, Parameter.Value<ServerLocation> locationParameter2, Database database) {
        super(locationParameter, locationParameter2, database);
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Player player = (Player) context.cause().root();
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
    }
}
