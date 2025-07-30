package net.slimediamond.espial.sponge.query;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.query.EspialQuery;
import net.slimediamond.espial.sponge.Espial;
import net.slimediamond.espial.sponge.utils.formatting.Format;
import net.slimediamond.espial.sponge.utils.formatting.RecordFormatter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.world.server.ServerLocation;

public class EspialQueries {

    public static void showRecords(final EspialQuery query, final Audience audience) {
        Espial.getInstance().getEspialService().query(query).thenAccept(records -> {
            if (records.isEmpty()) {
                audience.sendMessage(Format.NO_RECORDS_FOUND);
            } else {
                PaginationList.builder()
                        .title(Format.title("Lookup results"))
                        .padding(Format.PADDING)
                        .contents(RecordFormatter.formatRecords(records, true))
                        .sendTo(audience);
            }
        });
    }

    public static void queryRecords(final ServerLocation location, final Player player) {
        showRecords(EspialQuery.builder()
                .location(location)
                .audience(player)
                .build(), player);
    }

}
