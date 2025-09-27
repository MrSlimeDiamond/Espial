package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.record.ItemFrameChangeRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.server.ServerLocation;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class SpongeItemFrameChangeRecordFactory implements RecordFactory<ItemFrameChangeRecord> {

    @Override
    public ItemFrameChangeRecord create(@NotNull final EspialEvent event,
                                        @NotNull final ResultSet rs,
                                        final int id,
                                        @NotNull final Date date,
                                        @Nullable final UUID user,
                                        @NotNull final EntityType<?> entityType,
                                        @NotNull final ServerLocation location,
                                        final boolean rolledBack) throws SQLException {
        final String itemData = rs.getString("item");
        try {
            final ItemStackSnapshot item = ItemStack.builder()
                    .fromContainer(DataFormats.JSON.get().read(itemData))
                    .build().asImmutable();
            final ItemStackSnapshot original = event.equals(EspialEvents.ITEM_FRAME_REMOVE.get())
                    ? item
                    : ItemStackSnapshot.empty();
            final ItemStackSnapshot replacement = event.equals(EspialEvents.ITEM_FRAME_INSERT.get())
                    ? item
                    : ItemStackSnapshot.empty();
            return new SpongeItemFrameChangeRecord(id, date, user, entityType, location, event, rolledBack, original, replacement);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
