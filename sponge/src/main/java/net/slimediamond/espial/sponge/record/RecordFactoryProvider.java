package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.record.EspialRecord;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.registry.RegistryEntry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Supplier;

public class RecordFactoryProvider {

    private static final Map<EspialEvent, Supplier<RecordFactory<? extends EspialRecord>>> RECORD_TYPES = Map.of(
            EspialEvents.BREAK.get(), SpongeBlockRecordFactory::new,
            EspialEvents.PLACE.get(), SpongeBlockRecordFactory::new
    );

    @SuppressWarnings("unchecked")
    public static <T extends EspialRecord> T create(@NotNull final ResultSet rs) throws SQLException {
        final int type = rs.getInt("type");
        final EspialEvent event = EspialEvents.registry().streamEntries()
                .map(RegistryEntry::value)
                .filter(e -> e.getId() == type)
                .findFirst().orElseThrow(() ->
                        new IllegalStateException("No Espial event associated with event ID '" + type + "'"));
        return (T) RECORD_TYPES.get(event).get().create(event, rs).join();
    }

}
