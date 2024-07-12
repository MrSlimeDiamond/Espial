package net.slimediamond.espial.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.ActionType;
import net.slimediamond.espial.Database;
import net.slimediamond.espial.StoredBlock;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

public class LookupCommand implements CommandExecutor {
    Parameter.Value<ServerLocation> locationParameter;
    Parameter.Value<ServerLocation> locationParameter2 ;
    Database database;

    public LookupCommand(Parameter.Value<ServerLocation> locationParameter, Parameter.Value<ServerLocation> locationParameter2, Database database) {
        this.locationParameter = locationParameter;
        this.locationParameter2 = locationParameter2;
        this.database = database;
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        if (context.hasAny(locationParameter) && context.hasAny(locationParameter2)) { // Range lookup
            ServerLocation location = context.requireOne(locationParameter);
            ServerLocation location2 = context.requireOne(locationParameter2);

            try {
                this.lookupRange(location, location2, context);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return CommandResult.success();
        } if (context.hasAny(locationParameter)) { // Coordinates lookup
            ServerLocation location = context.requireOne(locationParameter);

            try {
                this.lookupBlock(location, context);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return CommandResult.success();
        } else { // Ray trace block (playing is looking at target)
            // get the block the player is targeting
            if (!(context.cause().root() instanceof  Player)) {
                context.sendMessage(Component.text("Only players can run this! (specify block coordinates)").color(NamedTextColor.RED));
                return CommandResult.success();
            }

            Player player = (Player) context.cause().root();

            Optional<RayTraceResult<LocatableBlock>> result = RayTrace.block().sourceEyePosition(player).direction(player).world(player.serverLocation().world()).limit(4).execute();

            if (result.isPresent()) {
                RayTraceResult<LocatableBlock> rayTrace = result.get();

                try {
                    this.lookupBlock(rayTrace.selectedObject().serverLocation(), context);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                context.sendMessage(Component.text("Could not detect a block, move closer, perhaps?").color(NamedTextColor.RED));
            }
        }

        return CommandResult.success();
    }

    private void lookupBlock(ServerLocation location, CommandContext context) throws SQLException {
        ArrayList<StoredBlock> blocks = database.queryBlock(location.world().key().formatted(), location.blockX(), location.blockY(), location.blockZ());
        PaginationList.Builder paginationListBuilder = PaginationList.builder()
                .title(Component.text("Block data at ")
                        .color(NamedTextColor.GREEN)
                        .append(Component.text(location.blockX() + " " + location.blockY() + " " + location.blockZ())
                                .color(NamedTextColor.YELLOW))
                        .append(Component.text(" in "))
                        .color(NamedTextColor.GREEN)
                        .append(Component.text(location.world().key().formatted())
                                .color(NamedTextColor.YELLOW))
                );

        ArrayList<Component> contents = this.generateContents(blocks);

        paginationListBuilder.contents(contents);
        paginationListBuilder.sendTo((Audience) context.cause().root());
    }

    protected void lookupRange(ServerLocation location, ServerLocation location2, CommandContext context) throws SQLException {
        ArrayList<StoredBlock> blocks = database.queryRange(location.blockX(), location.blockY(), location.blockZ(), location2.blockX(), location2.blockY(), location2.blockZ());

        PaginationList.Builder paginationListBuilder = PaginationList.builder()
                .title(Component.text("Block data between ")
                        .color(NamedTextColor.GREEN)
                        .append(Component.text(location.blockX() + " " + location.blockY() + " " + location.blockZ())
                                .color(NamedTextColor.YELLOW))
                        .append(Component.text(" and "))
                        .color(NamedTextColor.GREEN)
                        .append(Component.text(location2.blockX() + " " + location2.blockY() + " " + location2.blockZ())
                                .color(NamedTextColor.YELLOW))
                );

        ArrayList<Component> contents = this.generateContents(blocks);

        paginationListBuilder.contents(contents);
        paginationListBuilder.sendTo((Audience) context.cause().root());
    }

    protected ArrayList<Component> generateContents(ArrayList<StoredBlock> blocks) {

        ArrayList<Component> contents = new ArrayList<>();

        blocks.forEach(block -> {
            String name;
            if (block.user().isPresent()) {
                name = block.user().get().name();
            } else {
                // whenever players place blocks the server itself appears to register
                // a modification to it, so we should just ignore them
                // in our output to make things easier.
                if (block.actionType().equals(ActionType.MODIFY)) {
                    return;
                }
                name = "(server)";
            }

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");

            contents.add(Component.text("[").color(NamedTextColor.GRAY).append(Component.text("#" + block.uid()).color(NamedTextColor.GREEN)).append(Component.text("]").color(NamedTextColor.GRAY))
                    .append(Component.space())
                    .append(Component.text(dateFormat.format((new Date(block.time().getTime() * 1000)))))
                    .color(NamedTextColor.GRAY)
                    .append(Component.space())
                    .append(Component.text(name).color(NamedTextColor.YELLOW))
                    .append(Component.space())
                    .append(Component.text(block.actionType().humanReadableVerb()).color(NamedTextColor.GREEN))
                    .append(Component.space())
                    .append(Component.text(block.blockId()).color(NamedTextColor.YELLOW))
                    .clickEvent(ClickEvent.runCommand("/espial inspect " + block.uid()))
            );
        });

        return contents;
    }

}
