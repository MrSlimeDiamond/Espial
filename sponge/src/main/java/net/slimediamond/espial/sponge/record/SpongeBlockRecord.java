package net.slimediamond.espial.sponge.record;

import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.event.EspialEvents;
import net.slimediamond.espial.api.record.BlockRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Date;
import java.util.UUID;

public class SpongeBlockRecord extends SpongeEspialRecord implements BlockRecord {

    private final BlockSnapshot original;
    private final BlockSnapshot replacement;

    public SpongeBlockRecord(final int id,
                             @NotNull final Date date,
                             @Nullable final UUID user,
                             @NotNull final EntityType<?> entityType,
                             @NotNull final ServerLocation location,
                             @NotNull final EspialEvent event,
                             @NotNull final BlockSnapshot original,
                             @NotNull final BlockSnapshot replacement,
                             final boolean rolledBack) {
        super(id, date, user, entityType, location, event, rolledBack);
        this.original = original;
        this.replacement = replacement;
    }

    @Override
    public BlockSnapshot getReplacementBlock() {
        return replacement;
    }

    @Override
    public BlockSnapshot getOriginalBlock() {
        return original;
    }

    @Override
    public String getTarget() {
        if (getEvent().equals(EspialEvents.PLACE.get())
                || getEvent().equals(EspialEvents.GROWTH.get())) {
            return getReplacementBlock().state().type().key(RegistryTypes.BLOCK_TYPE).formatted();
        }
        return getOriginalBlock().state().type().key(RegistryTypes.BLOCK_TYPE).formatted();
    }

    @Override
    public void rollback() {
        original.restore(true, BlockChangeFlags.ALL);
    }

    @Override
    public void restore() {
        replacement.restore(true, BlockChangeFlags.ALL);
    }

}
