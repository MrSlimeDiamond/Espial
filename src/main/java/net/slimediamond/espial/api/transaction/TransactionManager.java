package net.slimediamond.espial.api.transaction;

public interface TransactionManager {
  /**
   * Add a transaction to a user
   *
   * @param key The identifier (user)
   * @param transaction The transaction to add
   */
  void add(Object key, EspialTransaction transaction);

  /**
   * Remove a transaction from a user
   *
   * @param key The identifier (user)
   * @param transaction The transaction to remove
   */
  void remove(Object key, EspialTransaction transaction);

  /**
   * Undo latest transaction of a user
   *
   * @param key The identifier (user)
   */
  int undo(Object key) throws Exception;

  /**
   * Redo the latest undone transaction of a user
   *
   * @param key The identifier (user)
   */
  int redo(Object key) throws Exception;
}
