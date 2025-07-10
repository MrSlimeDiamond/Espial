package net.slimediamond.espial.api.record;

import net.slimediamond.espial.api.event.EspialEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * A record used for storing data in Espial
 */
public interface EspialRecord {

    /**
     * Get the ID internally identifying this Espial record,
     * if present. Otherwise {@code -1}
     *
     * @return Record ID
     */
    int getId();

    /**
     * Get the date that this record occurred at
     *
     * @return Record date
     */
    Date getDate();

    /**
     * Get the user which caused the record to be created,
     * if present
     *
     * @return Record user
     */
    Optional<UUID> getUser();

    /**
     * Get the entity type which triggered this
     *
     * @return Entity type cause
     */
    EntityType<?> getEntityType();

    /**
     * Get the location of the record
     *
     * @return Record location
     */
    ServerLocation getLocation();

    /**
     * Get the event causing the record
     *
     * @return Record event
     */
    EspialEvent getEvent();

    /**
     * Roll back this record
     */
    void rollback();

    /**
     * Restore this record
     */
    void restore();

    /**
     * Get whether the record is rolled back
     *
     * @return Whether the record is rolled back
     */
    boolean isRolledBack();

    interface Builder extends org.spongepowered.api.util.Builder<EspialRecord, Builder> {

        /**
         * Sets the date the action happened at
         *
         *<p><strong>This is required</strong></p>
         *
         * @param date The date of the event
         * @return This builder, for chaining
         */
        Builder date(@NotNull Date date);

        /**
         * Sets the user which caused the action
         *
         * @param user The user cauase of the action
         * @return This builder, for chaining
         */
        Builder user(@NotNull UUID user);

        /**
         * Sets the entity type which caused the action
         *
         * @param entityType The entity type which caused the action
         * @return This builder, for chaining
         */
        Builder entityType(@NotNull EntityType<?> entityType);

        /**
         * Sets the location the action happened at
         *
         *<p><strong>This is required</strong></p>
         *
         * @param location The location of the action
         * @return This builder, for chaining
         */
        Builder location(@NotNull ServerLocation location);

        /**
         * Sets the event associated with the action
         *
         *<p><strong>This is required</strong></p>
         *
         * @param event The event
         * @return This builder, for chaining
         */
        Builder event(@NotNull EspialEvent event);

    }

}
