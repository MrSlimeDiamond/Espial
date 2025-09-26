package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.record.ContainerChangeRecord;
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

public class SpongeContainerChangeRecordFactory implements RecordFactory<ContainerChangeRecord> {

    @Override
    public ContainerChangeRecord create(@NotNull final EspialEvent event,
                                        @NotNull final ResultSet rs,
                                        final int id,
                                        @NotNull final Date date,
                                        @Nullable final UUID user,
                                        @NotNull final EntityType<?> entityType,
                                        @NotNull final ServerLocation location,
                                        final boolean rolledBack) throws SQLException {
        // might need to fix ResultSet column name
        try {
            final ItemStackSnapshot original = ItemStack.builder()
                    .fromContainer(DataFormats.JSON.get().read(rs.getString("item_original")))
                    .build()
                    .asImmutable();
            final ItemStackSnapshot replacement = ItemStack.builder()
                    .fromContainer(DataFormats.JSON.get().read(rs.getString("item_replacement")))
                    .build()
                    .asImmutable();
            final int slot = rs.getInt("slot");
            return new SpongeContainerChangeRecord(id, date, user, entityType, location, event, rolledBack, slot, original, replacement);
        } catch (IOException e) {
            throw new RuntimeException(e); // should not happen. TODO: Handle this better
        }
    }

}
