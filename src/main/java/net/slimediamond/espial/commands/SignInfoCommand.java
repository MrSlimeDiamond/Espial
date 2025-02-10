package net.slimediamond.espial.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.Espial;
import net.slimediamond.espial.StoredBlock;
import net.slimediamond.espial.util.BlockUtil;
import net.slimediamond.espial.util.DisplayNameUtil;
import net.slimediamond.espial.nbt.NBTDataParser;
import net.slimediamond.espial.util.RayTraceUtil;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class SignInfoCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        if (context.cause().root() instanceof Player player) {
            RayTraceUtil.getBlockFacingPlayer(player).ifPresentOrElse(block -> {
                if (BlockUtil.SIGNS.contains(block.blockState().type())) {
                    // player is looking at a sign
                    ServerLocation location = block.serverLocation();
                    String blockId = RegistryTypes.BLOCK_TYPE.get().valueKey(block.blockState().type()).formatted();

                    try {
                        ArrayList<StoredBlock> blocks = Espial.getInstance().getDatabase().queryBlock(location.world().key().formatted(), location.blockX(), location.blockY(), location.blockZ(), null, blockId, null);
                        StoredBlock target = blocks.get(0); // top index

                        Component name = DisplayNameUtil.getDisplayName(target);

                        var builder = Component.text().append(Espial.prefix.append(Component.text("That sign was placed by ").color(NamedTextColor.WHITE).append(name.color(NamedTextColor.YELLOW))));

                        var info = Component.text().append(Espial.prefix);

                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                        String date = dateFormat.format(target.time());

                        info.append(Component.newline());
                        info.append(Component.text("Date: ").color(NamedTextColor.DARK_AQUA).append(Component.text(date).color(NamedTextColor.WHITE)));

                        NBTDataParser.parseNBT(target).ifPresent(info::append);

                        builder.append(Component.text(" (...)").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(info)));
                        context.sendMessage(builder.build());

                    } catch (SQLException e) {
                        context.sendMessage(Espial.prefix.append(Component.text("SQLException. Not good. Tell an admin.").color(NamedTextColor.RED)));
                        throw new RuntimeException(e);
                    }

                } else {
                    context.sendMessage(Espial.prefix.append(Component.text("The block you are looking at is not a sign.").color(NamedTextColor.YELLOW)));
                }
            }, () -> {
                context.sendMessage(Espial.prefix.append(Component.text("Could not find the block you are looking at. Move closer, perhaps?").color(NamedTextColor.RED)));
            });
        } else {
            context.sendMessage(Component.text("This command can only be run by players"));
        }

        return CommandResult.success();
    }
}
