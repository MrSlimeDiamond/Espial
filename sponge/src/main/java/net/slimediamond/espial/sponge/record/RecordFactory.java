package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.world.server.ServerLocation;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public interface RecordFactory<T extends EspialRecord> {

    /**
     * Creates a {@link T} from the given input
     *
     * @param event The event
     * @param rs The SQL {@link ResultSet} for creation
     * @return Created record
     */
    T create(@NotNull EspialEvent event,
             @NotNull ResultSet rs,
             int id,
             @NotNull Date date,
             @Nullable UUID user,
             @NotNull EntityType<?> entityType,
             @NotNull ServerLocation location,
             boolean rolledBack) throws SQLException;

}
