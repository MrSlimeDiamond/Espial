package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialRecord;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface RecordFactory<T extends EspialRecord> {

    /**
     * Creates a {@link T} from the given input
     *
     * @param event The event
     * @param rs The SQL {@link ResultSet} for creation
     * @return Created record
     */
    T create(@NonNull EspialEvent event, @NotNull ResultSet rs) throws SQLException;

}
