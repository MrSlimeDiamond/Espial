package net.slimediamond.espial.sponge.record;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.slimediamond.espial.api.SignText;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.SignModifyRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.world.server.ServerLocation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class SpongeSignModifyRecordFactory implements RecordFactory<SignModifyRecord> {

    @Override
    public SignModifyRecord create(@NotNull final EspialEvent event,
                                   @NotNull final ResultSet rs,
                                   final int id,
                                   @NotNull final Date date,
                                   @Nullable final UUID user,
                                   @NotNull final EntityType<?> entityType,
                                   @NotNull final ServerLocation location,
                                   final boolean rolledBack)
            throws SQLException {

        // Fuck

        final Component originalFront1 = getComponent(rs.getString("original_front_1"));
        final Component originalFront2 = getComponent(rs.getString("original_front_2"));
        final Component originalFront3 = getComponent(rs.getString("original_front_3"));
        final Component originalFront4 = getComponent(rs.getString("original_front_4"));

        final Component originalBack1 = getComponent(rs.getString("original_back_1"));
        final Component originalBack2 = getComponent(rs.getString("original_back_2"));
        final Component originalBack3 = getComponent(rs.getString("original_back_3"));
        final Component originalBack4 = getComponent(rs.getString("original_back_4"));

        final Component replacementFront1 = getComponent(rs.getString("replacement_front_1"));
        final Component replacementFront2 = getComponent(rs.getString("replacement_front_2"));
        final Component replacementFront3 = getComponent(rs.getString("replacement_front_3"));
        final Component replacementFront4 = getComponent(rs.getString("replacement_front_4"));

        final Component replacementBack1 = getComponent(rs.getString("replacement_back_1"));
        final Component replacementBack2 = getComponent(rs.getString("replacement_back_2"));
        final Component replacementBack3 = getComponent(rs.getString("replacement_back_3"));
        final Component replacementBack4 = getComponent(rs.getString("replacement_back_4"));


        final SignText originalText = SignText.from(originalFront1, originalFront2, originalFront3, originalFront4,
                originalBack1, originalBack2, originalBack3, originalBack4);

        final SignText replacementText = SignText.from(replacementFront1, replacementFront2, replacementFront3, replacementFront4,
                replacementBack1, replacementBack2, replacementBack3, replacementBack4);

        final BlockState blockState = BlockState.fromString(rs.getString("state_original"));

        return new SpongeSignModifyRecord(date, user, entityType, location, event, rolledBack, originalText, replacementText, true, blockState);
    }

    private static Component getComponent(final String string) {
        if (string == null) {
            return Component.empty();
        }
        return GsonComponentSerializer.gson().deserialize(string);
    }

}
