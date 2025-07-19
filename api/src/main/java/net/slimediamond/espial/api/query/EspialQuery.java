package net.slimediamond.espial.api.query;

import net.kyori.adventure.audience.Audience;
import net.slimediamond.espial.api.event.EspialEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3i;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EspialQuery {

    /**
     * Get the date to query only for records <b>before</b>
     *
     * @return Before date
     */
    Optional<Date> getBefore();

    /**
     * Get the date to query only for records <b>after</b>
     *
     * @return After date
     */
    Optional<Date> getAfter();

    /**
     * Get a list of block types to query for
     *
     * @return Block types
     */
    List<BlockType> getBlockTypes();

    /**
     * Get a list of events to query for
     *
     * @return Events
     */
    List<EspialEvent> getEvents();

    /**
     * Get a list of users to query for
     *
     * @return Users
     */
    List<UUID> getUsers();

    /**
     * Get the minimum position of the query
     *
     * @return Minimum position
     */
    Vector3i getMinimumPosition();

    /**
     * Get the maximum position of the query
     *
     * @return Maximum position
     */
    Vector3i getMaximumPosition();

    /**
     * Get the location of the world to query in
     *
     * @return World {@link ResourceKey}
     */
    ResourceKey getWorldKey();

    /**
     * Get a callback to send errors or other messages to
     *
     * @return Query audience
     */
    Optional<Audience> getAudience();

    static Builder builder() {
        return Sponge.game().builderProvider().provide(Builder.class);
    }

    interface Builder extends org.spongepowered.api.util.Builder<EspialQuery, Builder> {

        /**
         * Set the date to query records before
         *
         * @param date The date to query before
         * @return This builder, for chaining
         */
        Builder before(@NotNull Date date);

        /**
         * Set the date to query records before
         *
         * @param before The date to query before
         * @return This builder, for chaining
         */
        default Builder before(@NotNull final Instant before) {
            return before(new Date(before.toEpochMilli()));
        }

        /**
         * Set the date to query records after
         *
         * @param after The date to query after
         * @return This builder, for chaining
         */
        default Builder after(@NotNull final Instant after) {
            return before(new Date(after.toEpochMilli()));
        }

        /**
         * Set the date to query records after
         *
         * @param date The date to query after
         * @return This builder, for chaining
         */
        Builder after(@NotNull Date date);

        /**
         * Add a block type to query for
         *
         * @param blockType The block type to query for
         * @return This builder, for chaining
         */
        Builder addBlockType(@NotNull BlockType blockType);

        /**
         * Add an event to query for
         *
         * @param event The event to query for
         * @return This builder, for chaining
         */
        Builder addEvent(@NotNull EspialEvent event);

        /**
         * Add a user to query for
         *
         * @param user The user to query for
         * @return This builder, for chaining
         */
        Builder addUser(@NotNull UUID user);

        /**
         * Set the minimum position to query at
         *
         * <p><strong>This is required</strong></p>
         *
         * @param minimum The minimum position
         * @return This builder, for chaining
         */
        Builder minimum(@NotNull Vector3i minimum);

        /**
         * Set the maximum position to query at
         *
         * <p><strong>This is required</strong></p>
         *
         * @param maximum The maximum position
         * @return This builder, for chaining
         */
        Builder maximum(@NotNull Vector3i maximum);

        /**
         * Set the world to query within
         *
         * <p><strong>This is required</strong></p>
         *
         * @param worldKey The {@link ResourceKey} of the world
         *                 to query inside of
         * @return This builder, for chaining
         */
        Builder worldKey(@NotNull ResourceKey worldKey);

        /**
         * Set the audience for the query
         *
         * @param audience The audience
         * @return This builder, for chaining
         */
        Builder audience(@NotNull Audience audience);

        /**
         * Query at a server location
         *
         * @param location The location to query at
         * @return This builder, for chaining
         */
        Builder location(@NotNull ServerLocation location);

    }

}
