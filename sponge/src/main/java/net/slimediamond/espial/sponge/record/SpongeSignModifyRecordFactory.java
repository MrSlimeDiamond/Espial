package net.slimediamond.espial.sponge.record;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.EspialSignModifyRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

public class SpongeSignModifyRecordFactory implements RecordFactory<EspialSignModifyRecord> {

    @Override
    public EspialSignModifyRecord create(@NotNull final EspialEvent event,
                                         @NotNull final ResultSet rs,
                                         final int id,
                                         @NotNull final Date date,
                                         @Nullable final UUID user,
                                         @NotNull final EntityType<?> entityType,
                                         @NotNull final ServerLocation location,
                                         final boolean rolledBack)
            throws SQLException {
        final JsonArray original = JsonParser.parseString(rs.getString("extra_original")).getAsJsonArray();
        final JsonArray replacement = JsonParser.parseString(rs.getString("extra_replacement")).getAsJsonArray();

        final List<Component> originalComponents = StreamSupport.stream(original.spliterator(), false)
                .map(SpongeSignModifyRecordFactory::getComponent)
                .toList();
        final List<Component> replacementComponents = StreamSupport.stream(replacement.spliterator(), false)
                .map(SpongeSignModifyRecordFactory::getComponent)
                .toList();

        final BlockState blockState = BlockState.fromString(rs.getString("state_original"));

        return new SpongeSignModifyRecord(date, user, entityType, location, event, rolledBack, originalComponents, replacementComponents, true, blockState);
    }

    private static Component getComponent(final JsonElement element) {
        if (element.getAsString().isEmpty()) {
            return Component.empty();
        }
        return GsonComponentSerializer.gson().deserialize(element.getAsString());
    }

}
