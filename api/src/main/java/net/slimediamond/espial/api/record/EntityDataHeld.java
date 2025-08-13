package net.slimediamond.espial.api.record;

import org.spongepowered.api.entity.EntityType;

import java.util.Optional;
import java.util.UUID;

/**
 * Something which holds the data of an entity, which could also be
 * a user
 */
public interface EntityDataHeld {

    /**
     * Get the entity type which triggered this
     *
     * @return Entity type cause
     */
    EntityType<?> getEntityType();


    /**
     * Get the user which caused the record to be created,
     * if present
     *
     * @return Record user
     */
    Optional<UUID> getUser();

}
