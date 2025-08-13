package net.slimediamond.espial.sponge.wand.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.wand.WandType;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import net.slimediamond.espial.sponge.utils.formatting.RecordFormatter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.Arrays;

public class DebugWand implements WandType {

    @Override
    public void apply(final EspialQuery query, final ServerPlayer player, final ItemStack itemStack) {
        Espial.getInstance().getEspialService().query(query).thenAccept(records -> {
            if (records.isEmpty()) {
                player.sendMessage(Format.NO_RECORDS_FOUND);
                return;
            }
            PaginationList.builder()
                    .contents(records.stream().map(record -> RecordFormatter.format(record)
                            .append(Component.text(" [D]").color(NamedTextColor.DARK_AQUA)
                                    .hoverEvent(HoverEvent.showText(
                                            Component.text()
                                                    .append(Format.detail("Type", record.getClass().getSimpleName()))
                                                    .appendNewline()
                                                    .append(Component.join(JoinConfiguration.newlines(),
                                                            Arrays.stream(record.getClass().getDeclaredFields()).map(field -> {
                                                                field.setAccessible(true);
                                                                Component value;
                                                                try {
                                                                    value = Component.text(field.get(record).toString());
                                                                } catch (final Exception e) {
                                                                    value = Component.text(e.getClass().getName()).color(Format.ERROR_COLOR);
                                                                    Espial.getInstance().getLogger().error(e);
                                                                }
                                                                return Format.detail(field.getName(), value);
                                                            }).toList())))))).toList())
                    .padding(Format.PADDING)
                    .title(Format.title("Logs + debug info @ ")
                            .append(Format.position(query.getMinimumPosition()).color(Format.ACCENT_COLOR)))
                    .sendTo(player);
        });
    }

}
