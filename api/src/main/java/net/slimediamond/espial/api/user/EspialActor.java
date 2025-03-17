package net.slimediamond.espial.api.user;

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
}
