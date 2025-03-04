package net.slimediamond.espial.api.action;

import net.slimediamond.espial.api.action.event.EventType;
import net.slimediamond.espial.api.user.EspialActor;

/**
 * An action done by a player
 *
 * @author SlimeDiamond
 */
public interface Action {
  /**
   * Get the actor of this action
   *
   * @return Actor
   */
  EspialActor getActor();

  /**
   * Get the X coordinate of the modified block.
   *
   * @return Block X
   */
  int getX();

  /**
   * Get the Y coordinate of the modified block.
   *
   * @return Block Y
   */
  int getY();

  /**
   * Get the Z coordinate of the modified block.
   *
   * @return Block Z
   */
  int getZ();

  /**
   * Get the world string (such as minecraft:overworld)
   *
   * @return World
   */
  String getWorld();

  /**
   * Get the type of action this is
   *
   * @return Action type
   */
  EventType getEventType();
}
