package net.slimediamond.espial.api.user;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.math.vector.Vector3d;

/**
 * The actor which did an {@link net.slimediamond.espial.api.action.Action}
 *
 * @author SlimeDiamond
 */
public interface EspialActor {
  /**
   * Get the actor's UUID Server is 0. Other entities are just their name.
   *
   * @return Actor UUID
   */
  String getUUID();

  /**
   * Whether the actor is a player
   *
   * @return If the actor is a player
   */
  boolean isPlayer();

  /**
   * Get the actor's position
   *
   * @return Actor position
   */
  @Nullable Vector3d getPosition();

  /**
   * Get the actor's rotation
   *
   * @return Actor rotation
   */
  @Nullable Vector3d getRotation();

  /**
   * Get the actor's item in hand
   *
   * @return Item in hand
   */
  String getItem();
}
