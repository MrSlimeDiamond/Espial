package net.slimediamond.espial.api.event;

/**
 * Manages events.
 *
 * @author SlimeDiamond
 */
public interface EventManager {
  /**
   * Call an event
   *
   * @param event Event to call
   */
  void callAll(AbstractEvent event);

  /**
   * Register a listener
   *
   * @param listener Instance to register
   */
  void register(Object listener);
}
