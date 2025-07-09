package net.slimediamond.espial.sponge.query;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.query.EspialQuery;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.math.vector.Vector3i;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpongeQuery implements EspialQuery {

    private final Date before;
    private final Date after;
    private final List<BlockType> blockTypes;
    private final List<EspialEvent> events;
    private final List<UUID> users;
    private final Vector3i minimumPosition;
    private final Vector3i maximumPosition;
    private final ResourceKey worldKey;
    private final Audience audience;

    public SpongeQuery(final Date before, final Date after, final List<BlockType> blockTypes, final List<EspialEvent> events,
                       final List<UUID> users, final Vector3i minimumPosition,
                       final Vector3i maximumPosition, final ResourceKey worldKey,
                       final Audience audience) {
        this.before = before;
        this.after = after;
        this.blockTypes = blockTypes;
        this.events = events;
        this.users = users;
        this.minimumPosition = minimumPosition;
        this.maximumPosition = maximumPosition;
        this.worldKey = worldKey;
        this.audience = audience;
    }

    @Override
    public Optional<Date> getBefore() {
        return Optional.ofNullable(before);
    }

    @Override
    public Optional<Date> getAfter() {
        return Optional.ofNullable(after);
    }

    @Override
    public List<BlockType> getBlockTypes() {
        return blockTypes;
    }

    @Override
    public List<EspialEvent> getEvents() {
        return events;
    }

    @Override
    public List<UUID> getUsers() {
        return users;
    }

    @Override
    public Vector3i getMinimumPosition() {
        return minimumPosition;
    }

    @Override
    public Vector3i getMaximumPosition() {
        return maximumPosition;
    }

    @Override
    public ResourceKey getWorldKey() {
        return worldKey;
    }

    @Override
    public Optional<Audience> getAudience() {
        return Optional.ofNullable(audience);
    }

}
