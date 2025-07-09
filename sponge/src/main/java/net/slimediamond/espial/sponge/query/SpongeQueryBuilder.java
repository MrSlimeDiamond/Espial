package net.slimediamond.espial.sponge.query;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.event.EspialEvent;
import net.slimediamond.espial.api.query.EspialQuery;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class SpongeQueryBuilder implements EspialQuery.Builder {

    private Date before;
    private Date after;
    private List<BlockType> blockTypes = new LinkedList<>();
    private List<EspialEvent> events = new LinkedList<>();
    private List<UUID> users = new LinkedList<>();
    private Vector3i minimumPosition;
    private Vector3i maximumPosition;
    private ResourceKey worldKey;
    private Audience audience;

    @Override
    public EspialQuery.Builder before(@NotNull final Date date) {
        this.before = date;
        return this;
    }

    @Override
    public EspialQuery.Builder after(@NotNull final Date date) {
        this.after = date;
        return this;
    }

    @Override
    public EspialQuery.Builder addBlockType(@NotNull final BlockType blockType) {
        this.blockTypes.add(blockType);
        return this;
    }

    @Override
    public EspialQuery.Builder addEvent(@NotNull final EspialEvent event) {
        this.events.add(event);
        return this;
    }

    @Override
    public EspialQuery.Builder addUser(@NotNull final UUID user) {
        this.users.add(user);
        return this;
    }

    @Override
    public EspialQuery.Builder minimum(@NotNull final Vector3i minimum) {
        this.minimumPosition = minimum;
        return this;
    }

    @Override
    public EspialQuery.Builder maximum(@NotNull final Vector3i maximum) {
        this.maximumPosition =  maximum;
        return this;
    }

    @Override
    public EspialQuery.Builder worldKey(@NotNull final ResourceKey worldKey) {
        this.worldKey = worldKey;
        return this;
    }

    @Override
    public EspialQuery.Builder audience(@NotNull final Audience audience) {
        this.audience = audience;
        return this;
    }

    @Override
    public EspialQuery.Builder location(@NotNull final ServerLocation location) {
        this.minimumPosition = location.blockPosition();
        this.maximumPosition = location.blockPosition();
        this.worldKey = location.worldKey();
        return this;
    }

    @Override
    public @NotNull EspialQuery build() {
        return new SpongeQuery(before, after, blockTypes, events, users, minimumPosition, maximumPosition, worldKey, audience);
    }

}
