package net.slimediamond.espial.sponge.wand.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.api.record.EspialRecord;
import net.slimediamond.espial.api.wand.WandType;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.data.EspialKeys;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.math.vector.Vector3i;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class StageWand implements WandType {

    @Override
    public void apply(final EspialQuery query, final ServerPlayer player, final ItemStack itemStack) {
        final boolean rollback = itemStack.getOrElse(EspialKeys.STAGE_ROLLS_BACK, true);
        Espial.getInstance().getEspialService().query(query).thenAccept(results -> {
            if (results.isEmpty()) {
                player.sendMessage(Format.NO_RECORDS_FOUND);
                return;
            }
            List<EspialRecord> records;
            if (rollback) {
                records = results.stream().sorted(Comparator.comparingInt(EspialRecord::getId).reversed()).toList();
            } else {
                records = results.stream().sorted(Comparator.comparingInt(EspialRecord::getId)).toList();
            }

            records = records.stream().filter(record -> record.isRolledBack() == !rollback).toList();
            final EspialRecord record = records.getFirst();
            if (record == null) {
                player.sendMessage(Format.NO_RECORDS_FOUND);
            } else {
                Sponge.server().scheduler().submit(Task.builder()
                        .execute(() -> {
                            if (rollback) {
                                record.rollback();
                            } else {
                                record.restore();
                            }
                        })
                        .plugin(Espial.getInstance().getContainer())
                        .build());
                try {
                    Espial.getInstance().getDatabase().setRolledBack(record, rollback);
                } catch (final SQLException e) {
                    player.sendMessage(Format.error(Component.text("Unable to set rolled back status, this might " +
                            "result in some weirdness in the future")));
                    return;
                }
                final TextComponent.Builder builder = Component.text();
                final Vector3i min = query.getMinimumPosition();
                final Vector3i max = query.getMaximumPosition();
                if (min.equals(max)) {
                    // one position - so just show that in chat
                    builder.append(Format.text("Block at "))
                            .append(Format.position(min).color(Format.ACCENT_COLOR))
                            .append(Component.text(" rolled " + (rollback ? "back" : "forward")))
                            .append(Component.text(" one action"));
                } else {
                    // not currently possible
                    builder.append(Format.text("Blocks between "))
                            .append(Format.position(min).color(Format.ACCENT_COLOR))
                            .append(Component.text(" and "))
                            .append(Format.position(max).color(Format.ACCENT_COLOR))
                            .append(Component.text(" rolled " + (rollback ? "back" : "forward")))
                            .append(Component.text(" one action"));
                }
                player.sendMessage(builder.build());
            }
        });
    }

}
