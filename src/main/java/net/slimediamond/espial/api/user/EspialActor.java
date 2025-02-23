package net.slimediamond.espial.api.user;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.math.vector.Vector3d;

public interface EspialActor {
    /**
     * Get the actor's UUID
     * Server is 0. Other entities are just their name.
     *
     * @return Actor UUID
     */
    String getUUID();

    /**
     * Get the actor's position
     *
     * @return Actor position
     */
    @Nullable
    Vector3d getPosition();

    /**
     * Get the actor's rotation
     *
     * @return Actor rotation
     */
    @Nullable
    Vector3d getRotation();

    /**
     * Get the actor's item in hand
     *
     * @return Item in hand
     */
    String getItem();
}
